package com.medapp.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Orchestrates the RAG pipeline:
 * 1. Retrieve relevant document chunks from Pinecone via Jina embeddings
 * 2. Send retrieved context + user question to Groq LLM
 * 3. Return generated answer
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final PineconeService pineconeService;
    private final GroqService groqService;

    public RagService(PineconeService pineconeService, GroqService groqService) {
        this.pineconeService = pineconeService;
        this.groqService = groqService;
    }

    public String invoke(String userInput) throws IOException {
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("User input cannot be empty.");
        }

        log.info("RAG pipeline invoked for query length={}", userInput.length());

        List<String> context = pineconeService.query(userInput);

        if (context.isEmpty()) {
            log.warn("No documents retrieved from Pinecone for query.");
            return "I couldn't find relevant medical information in the knowledge base. Please consult a healthcare professional for personalised advice.";
        }

        log.info("Retrieved {} context chunks from Pinecone.", context.size());

        String answer = groqService.generateAnswer(userInput, context);
        log.info("RAG pipeline completed successfully.");
        return answer;
    }
}
