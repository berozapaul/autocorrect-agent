package org.ai.autocorrect.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class SlackSignatureVerifier {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> parseJson(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse Slack JSON payload", e);
            return null;
        }
    }
}