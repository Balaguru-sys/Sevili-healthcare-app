package com.medapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}")    String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    /**
     * Uploads a file to Cloudinary under the medapp/records folder.
     * Returns the secure URL of the uploaded file.
     */
    @SuppressWarnings("unchecked")
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder",          "medapp/" + folder,
                    "resource_type",   "auto",    // handles PDF, image, raw
                    "use_filename",    true,
                    "unique_filename", true
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            String url = (String) result.get("secure_url");
            log.info("Cloudinary upload success: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Detects file type from content type for viewer routing.
     */
    public static String detectFileType(String contentType) {
        if (contentType == null) return "OTHER";
        if (contentType.contains("pdf"))              return "PDF";
        if (contentType.startsWith("image/"))         return "IMAGE";
        if (contentType.startsWith("text/"))          return "TEXT";
        return "OTHER";
    }
}
