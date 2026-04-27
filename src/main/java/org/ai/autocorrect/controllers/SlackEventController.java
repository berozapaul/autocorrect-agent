package org.ai.autocorrect.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ai.autocorrect.services.SlackAutoCorrectService;
import org.ai.autocorrect.services.SlackSignatureVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
public class SlackEventController {

    private final SlackAutoCorrectService autoCorrectService;
    private final SlackSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Slack slack = Slack.getInstance();

    @PostMapping("/events")
    public ResponseEntity<Map<String, String>> handleEvent(
            @RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Slack-Signature", required = false) String signature,
            @RequestBody String rawBody) {

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

    // --- Handle /fix or /refine ---
    @PostMapping("/commands")
    public ResponseEntity<String> handleCommand(
            @RequestParam("text") String text,
            @RequestParam("channel_id") String channelId,
            @RequestParam("user_id") String userId) {

        // Trigger the AI processing in the background
            autoCorrectService.processPreviewRequest(text, channelId, userId);

        // Return empty 200 immediately to acknowledge
        return ResponseEntity.ok("");
    }

    // --- Handle Button Clicks ---
    @PostMapping("/interactions")
    public ResponseEntity<String> handleInteractions(@RequestParam("payload") String payloadJson) throws Exception {
        JsonNode payload = objectMapper.readTree(payloadJson);
        String actionId = payload.path("actions").get(0).path("action_id").asText();
        String channelId = payload.path("channel").path("id").asText();
        String responseUrl = payload.path("response_url").asText(); // The unique URL to update/delete the preview

        if ("approve_and_post".equals(actionId)) {
            // 1. Get the text from the button value
            String correctedText = payload.path("actions").get(0).path("value").asText();

            // 2. Post the final message to the channel (as the user)
            autoCorrectService.postFinalMessage(channelId, correctedText);

            // 3. Force the ephemeral preview to disappear
            // We send a JSON payload to the response_url telling Slack to delete it
            deleteEphemeralMessage(responseUrl);

            return ResponseEntity.ok(""); // Acknowledge with a clean 200
        }

        if ("cancel_preview".equals(actionId)) {
            deleteEphemeralMessage(responseUrl);
            return ResponseEntity.ok("");
        }

        return ResponseEntity.ok("");
    }

    /**
     * Helper method to tell Slack to remove the ephemeral message via the response_url
     */
    private void deleteEphemeralMessage(String responseUrl) throws IOException {
        // Slack SDK's internal HTTP client is handy for this
        slack.getHttpClient().postJsonBody(responseUrl, "{\"delete_original\": \"true\"}");
    }
}
