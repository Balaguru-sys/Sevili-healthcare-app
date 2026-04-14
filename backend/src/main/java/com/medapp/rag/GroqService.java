package com.medapp.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private static final String SYSTEM_PROMPT =
            "You are a medical assistant specialized in answering health and medical questions. " +
            "Use only the following retrieved context to answer the question accurately. " +
            "If the answer is not found in the context, clearly state that you do not know. " +
            "Keep your answer concise — three sentences maximum. " +
            "Provide diagnoses and treatment suggestions for normal minor issues, but if the condition seems serious, advise consulting a doctor instead.\n\n";

    @Value("${groq.api.key}")
    private String groqKey;

    @Value("${groq.api.url}")
    private String groqUrl;

    @Value("${groq.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String generateAnswer(String question, List<String> context) throws IOException {
        String ctx = String.join("\n", context);
        String prompt = SYSTEM_PROMPT + ctx + "\n\nQuestion: " + question;

        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);

        ArrayNode messages = root.putArray("messages");
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        RequestBody body = RequestBody.create(root.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(groqUrl)
                .addHeader("Authorization", "Bearer " + groqKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String raw = response.body().string();
            log.debug("Groq response status: {}", response.code());

            JsonNode json = mapper.readTree(raw);

            if (!json.has("choices")) {
                throw new IOException("Invalid Groq response: " + raw);
            }

            JsonNode choices = json.get("choices");
            if (choices.isEmpty()) {
                throw new IOException("Groq returned empty choices");
            }

            JsonNode messageNode = choices.get(0).get("message");
            if (messageNode == null || !messageNode.has("content")) {
                throw new IOException("Groq response missing content");
            }

            return messageNode.get("content").asText();
        }
    }
}
