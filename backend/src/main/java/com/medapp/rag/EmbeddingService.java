package com.medapp.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class EmbeddingService {

    private static final String JINA_URL = "https://api.jina.ai/v1/embeddings";

    @Value("${jina.api.key}")
    private String jinaApiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Double> generateEmbedding(String text) throws IOException {
        String requestJson = mapper.createObjectNode()
                .put("model", "jina-embeddings-v2-base-en")
                .put("input", text)
                .toString();

        RequestBody body = RequestBody.create(requestJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(JINA_URL)
                .addHeader("Authorization", "Bearer " + jinaApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String raw = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Jina API error: " + response.code() + " -> " + raw);
            }
            JsonNode root = mapper.readTree(raw);
            JsonNode embeddingNode = root.get("data").get(0).get("embedding");
            List<Double> vector = new ArrayList<>();
            for (JsonNode v : embeddingNode) {
                vector.add(v.asDouble());
            }
            return vector;
        }
    }

    public List<float[]> embedBatch(List<String> texts) throws IOException {
        String requestJson = mapper.createObjectNode()
                .put("model", "jina-embeddings-v2-base-en")
                .putPOJO("input", texts)
                .toString();

        RequestBody body = RequestBody.create(requestJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(JINA_URL)
                .addHeader("Authorization", "Bearer " + jinaApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Jina API error: " + response.code());
            }
            String raw = response.body().string();
            JsonNode root = mapper.readTree(raw);
            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : root.get("data")) {
                JsonNode embeddingNode = item.get("embedding");
                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }
                embeddings.add(vector);
            }
            return embeddings;
        }
    }
}
