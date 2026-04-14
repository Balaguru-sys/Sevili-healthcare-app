package com.medapp.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * One-time ingestion helper: loads PDFs, splits into chunks, upserts to Pinecone.
 * Triggered via POST /api/admin/ingest — not exposed in production without auth.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 20;
    private static final int UPSERT_BATCH = 100;

    @Value("${pinecone.api.key}")
    private String pineconeApiKey;

    @Value("${pinecone.index.host}")
    private String indexHost;

    private final EmbeddingService embeddingService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngestionService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public int ingestDirectory(String dataDir) throws IOException {
        List<String[]> docs = loadPdfDirectory(dataDir);
        List<String[]> chunks = splitIntoChunks(docs);
        upsertToPinecone(chunks);
        return chunks.size();
    }

    private List<String[]> loadPdfDirectory(String dataDir) throws IOException {
        List<String[]> documents = new ArrayList<>();
        Path dirPath = Path.of(dataDir);

        if (!Files.exists(dirPath)) {
            throw new IOException("Directory not found: " + dataDir);
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            List<Path> pdfFiles = paths
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .toList();

            for (Path pdfPath : pdfFiles) {
                log.info("Loading PDF: {}", pdfPath.getFileName());

                try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();

                    for (int page = 1; page <= doc.getNumberOfPages(); page++) {
                        stripper.setStartPage(page);
                        stripper.setEndPage(page);
                        String text = stripper.getText(doc);

                        if (text != null && !text.isBlank()) {
                            documents.add(new String[]{text.trim(), pdfPath.toString()});
                        }
                    }
                }
            }
        }

        log.info("Loaded {} pages from PDFs", documents.size());
        return documents;
    }

    private List<String[]> splitIntoChunks(List<String[]> documents) {
        List<String[]> chunks = new ArrayList<>();

        for (String[] doc : documents) {
            String text = doc[0];
            for (int i = 0; i < text.length(); i += CHUNK_SIZE - CHUNK_OVERLAP) {
                int end = Math.min(i + CHUNK_SIZE, text.length());
                chunks.add(new String[]{text.substring(i, end), doc[1]});
            }
        }

        log.info("Generated {} chunks", chunks.size());
        return chunks;
    }

    private void upsertToPinecone(List<String[]> chunks) throws IOException {
        log.info("Upserting {} chunks to Pinecone", chunks.size());

        for (int start = 0; start < chunks.size(); start += UPSERT_BATCH) {
            int end = Math.min(start + UPSERT_BATCH, chunks.size());
            List<String[]> batch = chunks.subList(start, end);

            List<String> texts = batch.stream().map(c -> c[0]).toList();
            List<float[]> embeddings = embeddingService.embedBatch(texts);

            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode vectors = objectMapper.createArrayNode();

            for (int i = 0; i < batch.size(); i++) {
                ObjectNode vector = objectMapper.createObjectNode();
                vector.put("id", UUID.randomUUID().toString());

                ArrayNode values = objectMapper.createArrayNode();
                for (float v : embeddings.get(i)) {
                    values.add(v);
                }
                vector.set("values", values);

                ObjectNode metadata = objectMapper.createObjectNode();
                metadata.put("text", batch.get(i)[0]);
                metadata.put("source", batch.get(i)[1]);
                vector.set("metadata", metadata);

                vectors.add(vector);
            }

            requestBody.set("vectors", vectors);

            Request request = new Request.Builder()
                    .url("https://" + indexHost + "/vectors/upsert")
                    .header("Api-Key", pineconeApiKey)
                    .header("X-Pinecone-API-Version", "2024-07")
                    .post(RequestBody.create(
                            objectMapper.writeValueAsString(requestBody),
                            MediaType.parse("application/json")
                    ))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String err = response.body() != null ? response.body().string() : "no body";
                    throw new IOException("Pinecone upsert failed: " + err);
                }
            }

            log.info("Upserted {}/{} chunks", end, chunks.size());
        }

        log.info("Pinecone upsert complete");
    }
}
