package com.medapp.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PineconeService {

    @Value("${pinecone.api.key}")
    private String pineconeApiKey;

    @Value("${pinecone.index.host}")
    private String indexHost;

    @Value("${pinecone.top-k:3}")
    private int topK;

    private final EmbeddingService embeddingService;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public PineconeService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public List<String> query(String question) throws IOException {
        List<Double> vector = embeddingService.generateEmbedding(question);

        RequestBody body = RequestBody.create(
                mapper.writeValueAsString(
                        mapper.createObjectNode()
                                .putPOJO("vector", vector)
                                .put("topK", topK)
                                .put("includeMetadata", true)
                ),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://" + indexHost + "/query")
                .header("Api-Key", pineconeApiKey)
                .header("X-Pinecone-API-Version", "2024-07")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String raw = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Pinecone error: " + raw);
            }
            JsonNode json = mapper.readTree(raw);
            return json.findValuesAsText("text");
        }
    }
}
