package org.ai.autocorrect.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ai.autocorrect.services.SlackAutoCorrectService;
import org.ai.autocorrect.services.SlackSignatureVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
public class SlackEventController {

    private final SlackAutoCorrectService autoCorrectService;
    private final SlackSignatureVerifier signatureVerifier;

    @PostMapping("/events")
    public ResponseEntity<Map<String, String>> handleEvent(
            @RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Slack-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        // ── 1. Verify request is genuinely from Slack ──
//        if (!signatureVerifier.isValid(timestamp, signature, rawBody)) {
//            log.warn("Invalid Slack signature — request rejected");
//            return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
//        }

        // ── 2. Parse raw body into map ──
        Map<String, Object> payload = signatureVerifier.parseJson(rawBody);
        if (payload == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid JSON"));
        }

        String type = (String) payload.get("type");

        // ── 3. Handle Slack's one-time URL verification ──
        if ("url_verification".equals(type)) {
            log.info("Slack URL verification challenge received");
            return ResponseEntity.ok(Map.of("challenge", (String) payload.get("challenge")));
        }

        // ── 4. Handle message events asynchronously ──
        if ("event_callback".equals(type)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = (Map<String, Object>) payload.get("event");
            if (event != null) {
                autoCorrectService.processAsync(event);
            }
        }

        // ── 5. Always return 200 immediately (Slack requires < 3s) ──
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
