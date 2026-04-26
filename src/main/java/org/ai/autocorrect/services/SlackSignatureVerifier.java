package org.ai.autocorrect.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class SlackSignatureVerifier {

//    @Value("${slack.signing.secret}")
//    private String signingSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Reject requests older than 5 minutes (replay attack protection)
//    private static final long MAX_AGE_SECONDS = 300;

//    public boolean isValid(String timestamp, String signature, String rawBody) {
//        try {
//            if (timestamp == null || signature == null || rawBody == null) return false;
//
//            // Check request age
//            long requestTime = Long.parseLong(timestamp);
//            long now = Instant.now().getEpochSecond();
//            if (Math.abs(now - requestTime) > MAX_AGE_SECONDS) {
//                log.warn("Slack request timestamp too old — possible replay attack");
//                return false;
//            }
//
//            // Compute HMAC-SHA256 signature
//            String baseString = "v0:" + timestamp + ":" + rawBody;
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(new SecretKeySpec(
//                    signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
//            byte[] hash = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
//
//            StringBuilder hexBuilder = new StringBuilder("v0=");
//            for (byte b : hash) {
//                hexBuilder.append(String.format("%02x", b));
//            }
//
//            return hexBuilder.toString().equals(signature);
//
//        } catch (Exception e) {
//            log.error("Signature verification error", e);
//            return false;
//        }
//    }

    public Map<String, Object> parseJson(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse Slack JSON payload", e);
            return null;
        }
    }
}