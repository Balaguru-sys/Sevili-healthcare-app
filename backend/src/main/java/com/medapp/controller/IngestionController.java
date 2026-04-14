package com.medapp.controller;

import com.medapp.rag.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-only endpoint to ingest medical PDFs into Pinecone.
 * Run once before going live. Protect this endpoint with an admin role in SecurityConfig.
 *
 * Usage: POST /api/admin/ingest
 *   Body: { "dataDir": "data" }
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestBody Map<String, String> body) throws Exception {

        String dataDir = body.getOrDefault("dataDir", "data");
        int chunks = ingestionService.ingestDirectory(dataDir);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "chunksUpserted", chunks,
                "dataDir", dataDir
        ));
    }
}
