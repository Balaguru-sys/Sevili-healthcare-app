package com.medapp.controller;

import com.medapp.auth.AuthenticatedUser;
import com.medapp.dto.ChatDto;
import com.medapp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<ChatDto.MessageResponse> sendMessage(
            @Valid @RequestBody ChatDto.MessageRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Always use the authenticated user's ID for patient role
        if ("ROLE_PATIENT".equals(principal.getRole())) {
            req.setPatientId(principal.getId());
        }
        return ResponseEntity.ok(chatService.processMessage(req));
    }

    @GetMapping("/history/{patientId}")
    public ResponseEntity<List<ChatDto.HistoryItem>> history(
            @PathVariable Long patientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Patients can only fetch their own history
        if ("ROLE_PATIENT".equals(principal.getRole())
                && !principal.getId().equals(patientId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(chatService.getHistory(patientId));
    }
}
