package org.ai.autocorrect.services;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackAutoCorrectService {

    private final ChatClient chatClient;

    @Value("${slack.user.token}")
    private String slackUserToken;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    private final Slack slack = Slack.getInstance();


    @Async("taskExecutor")
    public void processAsync(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("type");
            String subtype   = (String) event.get("subtype");
            String botId     = (String) event.get("bot_id");
            String text      = (String) event.get("text");
            String channel   = (String) event.get("channel");
            String ts        = (String) event.get("ts");

            // ── Guard clauses — prevent loops and skip invalid events ──
            if (!"message".equals(eventType))       return; // not a message event
            if (subtype != null)                    return; // edited/bot/system → skip to avoid loops
            if (botId != null)                      return; // posted by a bot
            if (text == null || text.isBlank())     return; // nothing to correct
            if (text.length() < 5)                  return; // too short to bother

            log.info("Processing message — channel={} ts={}", channel, ts);

            // ── Call Gemini to correct the text ──
            String corrected = correctWithGemini(text);

            // ── Skip if nothing changed ──
            if (corrected == null || corrected.trim().equalsIgnoreCase(text.trim())) {
                log.info("No correction needed — skipping update");
                return;
            }

            // ── Update the original Slack message in-place ──
            MethodsClient methods = slack.methods(slackUserToken);
            ChatUpdateResponse response = methods.chatUpdate(r -> r
                    .channel(channel)
                    .ts(ts)
                    .text(corrected.trim())
            );

            if (response.isOk()) {
                log.info("Message corrected — channel={}", channel);
            } else {
                log.error("Slack chatUpdate failed — error={}", response.getError());
            }

        } catch (Exception e) {
            log.error("Error processing Slack event", e);
        }
    }

    @Async("taskExecutor")
    public void processPreviewRequest(String text, String channel, String userId) {
        try {
            String corrected = correctWithGemini(text);

            if (corrected == null) return;

            log.info("Attempting ephemeral post: Channel={}, User={}, Token={}", channel, userId, slackBotToken.substring(0, 10) + "...");

            // Build the Block Kit UI with buttons
            MethodsClient methods = slack.methods(slackBotToken);
            ChatPostEphemeralResponse response = methods.chatPostEphemeral(r -> r
                    .channel(channel)
                    .text("AI Suggestion Preview")
                    .user(userId)
                    .blocks(List.of(
                            SectionBlock.builder()
                                    .text(MarkdownTextObject.builder()
                                            .text("*Agent Suggestion:*\n" + corrected.trim())
                                            .build())
                                    .build(),
                            ActionsBlock.builder()
                                    .elements(List.of(
                                            ButtonElement.builder()
                                                    .text(PlainTextObject.builder().text("Post to Channel").build())
                                                    .style("primary")
                                                    .actionId("approve_and_post")
                                                    .value(corrected.trim()) // Store the text in the button
                                                    .build(),
                                            ButtonElement.builder()
                                                    .text(PlainTextObject.builder().text("Ignore").build())
                                                    .actionId("cancel_preview")
                                                    .build()
                                    ))
                                    .build()
                    ))
            );

            if (!response.isOk()) {
                log.error("Slack API Error: {}", response.getError());
                // Common errors:
                // 'channel_not_found' -> Bot isn't in the channel
                // 'user_not_in_channel' -> The userId isn't actually in that channel
            } else {
                log.info("Ephemeral message sent successfully!");
            }
        } catch (Exception e) {
            log.error("Error sending preview", e);
        }
    }

    // --- 2. THE FINAL POST ---
    public void postFinalMessage(String channel, String text) {
        try {
            // Using User Token so it appears as the user, not a bot
            slack.methods(slackUserToken).chatPostMessage(r -> r
                    .channel(channel)
                    .text(text)
            );
        } catch (Exception e) {
            log.error("Error posting final message", e);
        }
    }

    private String correctWithGemini(String originalText) {
        try {
            return chatClient.prompt()
                    .user("""
                    Correct the following Slack message. Fix all typos, grammar mistakes,
                    and make it sound professional and clear.
                    Return ONLY the corrected message — no explanations, no quotes:

                    """ + originalText)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return null;
        }
    }
}