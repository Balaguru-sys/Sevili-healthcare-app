package com.medapp.service;

import com.medapp.dto.ChatDto;
import com.medapp.dto.MedicalRecordDto;
import com.medapp.dto.VitalsDto;
import com.medapp.model.ChatMessage;
import com.medapp.model.Patient;
import com.medapp.rag.RagService;
import com.medapp.repository.ChatMessageRepository;
import com.medapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RagService ragService;
    private final ChatMessageRepository chatMessageRepository;
    private final PatientRepository patientRepository;
    private final VitalsService vitalsService;
    private final MedicalRecordService medicalRecordService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final List<String> DOCTOR_CONCERN_KEYWORDS = List.of(
            "chest pain", "difficulty breathing", "shortness of breath", "severe pain",
            "heart attack", "stroke", "unconscious", "bleeding heavily", "high fever",
            "cannot breathe", "dizzy", "fainting", "allergic reaction", "seizure",
            "vomiting blood", "sudden vision loss", "severe headache", "numbness",
            "irregular heartbeat", "swelling", "infection"
    );

    private static final java.util.Map<String, String> TANGLISH_PATTERNS = java.util.Map.of(
            "naan", "ta", "enaku", "ta", "iruku", "ta", "vali", "ta", "ungaluku", "ta",
            "romba", "ta", "ellam", "ta", "enna", "ta", "epdi", "ta", "nan", "ta"
    );

    @Transactional
    public ChatDto.MessageResponse processMessage(ChatDto.MessageRequest request) {

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient not found: " + request.getPatientId()));

        String detectedLang = detectLanguage(request.getMessage(), request.getLanguage());
        String lang = detectedLang != null ? detectedLang : "EN";

        ChatMessage userMsg = ChatMessage.builder()
                .patient(patient)
                .role(ChatMessage.Role.USER)
                .content(request.getMessage())
                .language(lang)
                .timestamp(LocalDateTime.now())
                .build();

        chatMessageRepository.save(userMsg);

        String enrichedPrompt = buildEnrichedPrompt(request.getMessage(), lang, request.getPatientId());

        String answer;

        try {
            answer = ragService.invoke(enrichedPrompt);
        } catch (Exception e) {
            log.error("RAG error for patient {}: {}", request.getPatientId(), e.getMessage());
            answer = getServiceUnavailableMessage(lang);
        }

        boolean consultDoctor = shouldConsultDoctor(request.getMessage(), answer);

        // 🔥 Ensure better UX: append doctor suggestion only when necessary
        if (consultDoctor) {
            answer += "\n\n⚠️ Please consider consulting a doctor if symptoms are severe or persist.";
        }

        ChatMessage botMsg = ChatMessage.builder()
                .patient(patient)
                .role(ChatMessage.Role.ASSISTANT)
                .content(answer)
                .language(lang)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(botMsg);

        return ChatDto.MessageResponse.builder()
                .response(answer)
                .language(lang)
                .messageId(saved.getId())
                .timestamp(saved.getTimestamp().format(TIME_FMT))
                .consultDoctor(consultDoctor)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatDto.HistoryItem> getHistory(Long patientId) {

        patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        return chatMessageRepository.findByPatientIdOrderByTimestampAsc(patientId)
                .stream()
                .map(m -> ChatDto.HistoryItem.builder()
                        .id(m.getId())
                        .role(m.getRole().name().toLowerCase())
                        .content(m.getContent())
                        .timestamp(m.getTimestamp().format(TIME_FMT))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatDto.AiContext getAiContext(Long patientId) {

        List<VitalsDto.Response> vitals = vitalsService.getVitalsHistory(patientId);
        List<MedicalRecordDto.Response> records = medicalRecordService.getRecordsForPatient(patientId);

        return ChatDto.AiContext.builder()
                .latestVitals(vitals.isEmpty() ? null : vitals.get(0))
                .records(records)
                .build();
    }

    private String buildEnrichedPrompt(String userMessage, String lang, Long patientId) {

        StringBuilder sb = new StringBuilder();

        sb.append(getLanguageInstruction(lang)).append("\n\n");

        boolean askVitals = userMessage.toLowerCase().matches(
                ".*(heart|heartbeat|bp|pressure|pulse|oxygen|spo2|sleep|vital|sugar|glucose|temperature|fever).*"
        );

        try {

            if (askVitals) {

                List<VitalsDto.Response> vitals = vitalsService.getVitalsHistory(patientId);

                if (!vitals.isEmpty()) {

                    VitalsDto.Response latest = vitals.get(0);

                    sb.append("PATIENT VITALS (most recent):\n");
                    sb.append("Heart Rate: ").append(latest.getHeartRate()).append(" bpm\n");
                    sb.append("Blood Pressure: ").append(latest.getBloodPressure()).append("\n");
                    sb.append("Oxygen Saturation: ").append(latest.getOxygenSaturation()).append("%\n");
                    sb.append("Sleep: ").append(latest.getSleepHours()).append("h ").append(latest.getSleepMinutes()).append("m\n");
                    sb.append("Recorded: ").append(latest.getRecordedAt()).append("\n\n");
                }
            }

            List<MedicalRecordDto.Response> records = medicalRecordService.getRecordsForPatient(patientId);

            if (!records.isEmpty()) {

                sb.append("PATIENT MEDICAL RECORDS:\n");

                records.forEach(r ->
                        sb.append("- [")
                                .append(r.getType())
                                .append("] ")
                                .append(r.getTitle())
                                .append(" (")
                                .append(r.getUploadedAt())
                                .append(")\n")
                );

                sb.append("\n");
            }

        } catch (Exception e) {
            log.warn("Could not load patient context for chat: {}", e.getMessage());
        }

        // 🔥 Dynamic guidance based on severity
        if (shouldConsultDoctor(userMessage, "")) {
            sb.append("IMPORTANT: This may be a serious condition. Advise doctor consultation.\n\n");
        } else {
            sb.append("IMPORTANT: This seems like a mild issue. Provide home remedies, lifestyle advice, and OTC suggestions. Avoid unnecessary doctor recommendation.\n\n");
        }

        sb.append("PATIENT QUESTION: ").append(userMessage);

        return sb.toString();
    }

    private String getLanguageInstruction(String lang) {

        String base = """
You are a smart and practical digital health assistant.

Your job is to:
- Understand the patient's problem clearly.
- Provide helpful, safe, and practical advice.

Guidelines:
1. For mild symptoms:
   - Suggest home remedies.
   - Suggest common over-the-counter medicines.
   - Give lifestyle advice.

2. For moderate symptoms:
   - Suggest precautions and monitoring.

3. For severe symptoms:
   - Recommend consulting a doctor.

Rules:
- Do NOT jump to doctor consultation for minor issues.
- ALWAYS provide at least one actionable suggestion.
- Keep answers short and clear.
- Do NOT prescribe strong medicines.
""";

        return switch (lang) {

            case "TA" -> base + "\nRespond ONLY in Tamil script (தமிழ்). Do not use English.";
            case "HI" -> base + "\nRespond ONLY in Hindi (हिंदी).";
            case "ML" -> base + "\nRespond ONLY in Malayalam (മലയാളം).";
            case "TE" -> base + "\nRespond ONLY in Telugu (తెలుగు).";
            default -> base + "\nRespond in clear English.";
        };
    }

    private String detectLanguage(String message, String requestedLang) {

        if (message == null) return requestedLang;

        String lower = message.toLowerCase();

        long tanglishMatches = TANGLISH_PATTERNS.keySet()
                .stream()
                .filter(lower::contains)
                .count();

        if (tanglishMatches >= 3) return "TA";

        if (message.matches(".*[\u0B80-\u0BFF].*")) return "TA";
        if (message.matches(".*[\u0900-\u097F].*")) return "HI";
        if (message.matches(".*[\u0D00-\u0D7F].*")) return "ML";
        if (message.matches(".*[\u0C00-\u0C7F].*")) return "TE";

        return requestedLang;
    }

    private String getServiceUnavailableMessage(String lang) {

        return switch (lang) {

            case "TA" -> "மன்னிக்கவும், மருத்துவ சேவை தற்போது கிடைக்கவில்லை. சற்று நேரம் கழித்து மீண்டும் முயலவும்.";
            case "HI" -> "क्षमा करें, चिकित्सा सेवा अभी उपलब्ध नहीं है। कृपया बाद में पुनः प्रयास करें।";
            case "ML" -> "ക്ഷമിക്കണം, ഇപ്പോൾ ആരോഗ്യ സേവനം ലഭ്യമല്ല. ദയവായി പിന്നീട് ശ്രമിക്കുക.";
            case "TE" -> "క్షమించండి, ఆరోగ్య సేవ ఇప్పుడు అందుబాటులో లేదు. దయచేసి తర్వాత మళ్ళీ ప్రయత్నించండి.";
            default -> "The medical knowledge service is temporarily unavailable. Please try again shortly.";
        };
    }

    private boolean shouldConsultDoctor(String userMessage, String aiResponse) {

        String combined = (userMessage + " " + aiResponse).toLowerCase();

        return DOCTOR_CONCERN_KEYWORDS
                .stream()
                .anyMatch(combined::contains);
    }
}