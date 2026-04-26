# ✍️ Slack Autocorrect Agent

An AI-powered Slack assistant that **silently rewrites your messages** — fixing typos, polishing grammar, and elevating tone to sound clear and professional. Built with **Spring Boot 3**, **Spring AI**, and **Google Gemini 2.5**.

---

## 🌟 What It Does

Stop second-guessing every Slack message. This agent integrates directly with your Slack workspace and offers two modes of operation:

1. **🤫 Silent Auto-Correct** — Listens to messages you post and rewrites them in-place using your User Token, so the corrected version appears as if you typed it perfectly the first time.
2. **👀 Preview-Before-Post** — Trigger a slash command (e.g. `/fix`) to get an **AI suggestion** as an ephemeral message with **Post to Channel** / **Ignore** buttons. Nothing is published until you approve it.

Powered by Gemini 2.5 Flash Lite for fast, low-cost rewrites.

---

## 🏗️ Architecture

| Layer | Component | Responsibility |
| :--- | :--- | :--- |
| **Entry** | `SlackEventController` | Handles Slack `/events`, `/commands`, and `/interactions` (button clicks) |
| **Security** | `SlackSignatureVerifier` | HMAC-SHA256 verification of Slack request signatures (replay-attack protection) |
| **Brain** | `SlackAutoCorrectService` | Async pipeline: guard clauses → Gemini call → Slack `chat.update` / ephemeral preview |
| **AI** | `AiConfig` | Configures the `ChatClient` with the professional-rewriter system prompt |
| **Concurrency** | `AsyncConfig` | Dedicated `ThreadPoolTaskExecutor` so Slack always gets a `<3s` response |

### Request Flow

```
Slack Event ──► /slack/events ──► Verify Signature ──► Async Worker
                                                          │
                                                          ▼
                                                    Gemini 2.5 Rewrite
                                                          │
                                                          ▼
                                            chat.update (silent rewrite)
                                                  OR
                                            chatPostEphemeral (preview + buttons)
```

---

## 🚀 Getting Started

### 1. Prerequisites
* **Java 21+** and **Maven 3.8+**
* A **Slack App** with Bot + User OAuth tokens
* A **Google Gemini API Key** ([Google AI Studio](https://aistudio.google.com/))
* A public HTTPS tunnel (e.g. [ngrok](https://ngrok.com/)) for local Slack callbacks

### 2. Configure Slack App

In your Slack App settings, enable:

| Feature | Setting |
| :--- | :--- |
| **Event Subscriptions** | Request URL: `https://<your-tunnel>/slack/events` · Subscribe to `message.channels` |
| **Slash Commands** | e.g. `/fix` → `https://<your-tunnel>/slack/commands` |
| **Interactivity** | Request URL: `https://<your-tunnel>/slack/interactions` |
| **Bot Token Scopes** | `chat:write`, `commands` |
| **User Token Scopes** | `chat:write` (required to edit messages as the user) |

### 3. Set Environment Variables

**macOS / Linux:**
```bash
export GEMINI_API_KEY='your_gemini_api_key'
export USER_TOKEN_SLACK='xoxp-...'   # User token (for silent in-place edits)
export BOT_TOKEN_SLACK='xoxb-...'    # Bot token (for ephemeral previews)
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY='your_gemini_api_key'
$env:USER_TOKEN_SLACK='xoxp-...'
$env:BOT_TOKEN_SLACK='xoxb-...'
```

### 4. Build and Run

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. Expose it with ngrok:

```bash
ngrok http 8080
```

Then paste the public URL into your Slack App's Event/Interactivity/Command settings.

---

## 🧪 Try It Out

| Scenario | What You Type | What Happens |
| :--- | :--- | :--- |
| **Silent rewrite** | `hey can u snd me teh report asap thx` | Message is silently updated to: *"Hey, could you send me the report as soon as possible? Thanks."* |
| **Preview mode** | `/fix this is super urgnt plz reveiw` | Ephemeral suggestion appears with **Post to Channel** / **Ignore** buttons |
| **No-op** | `ok` | Skipped (too short, nothing to correct) |
| **Loop guard** | Edited / bot / system messages | Skipped (prevents infinite correction loops) |

---

## ⚙️ Configuration Reference

`src/main/resources/application.properties`

| Property | Description |
| :--- | :--- |
| `spring.ai.google.genai.api-key` | Gemini API key (`${GEMINI_API_KEY}`) |
| `spring.ai.google.genai.chat.options.model` | Default: `gemini-2.5-flash-lite` |
| `spring.ai.google.genai.chat.options.temperature` | Default: `0.7` |
| `slack.user.token` | Slack User OAuth token (silent rewrites) |
| `slack.bot.token` | Slack Bot OAuth token (ephemeral previews) |
| `async.core-pool-size` / `async.max-pool-size` / `async.queue-capacity` | Async worker tuning |

---

## 📂 Key Files

| File | Responsibility |
| :--- | :--- |
| `SlackEventController.java` | REST endpoints for Slack events, slash commands, and interactive button payloads |
| `SlackAutoCorrectService.java` | Async correction pipeline + Slack API calls (`chatUpdate`, `chatPostEphemeral`, `chatPostMessage`) |
| `SlackSignatureVerifier.java` | Verifies `X-Slack-Signature` header (HMAC-SHA256) — currently commented; enable for production |
| `AiConfig.java` | `ChatClient` bean with the professional-rewriter system prompt |
| `AsyncConfig.java` | Thread pool executor so Slack always gets an instant ack |
| `index.html` | Minimal landing page served at `/` |

---

## 🛡️ Security Notes

* **Enable signature verification before production.** The HMAC check in `SlackSignatureVerifier` is commented out for local development — uncomment it and set `slack.signing.secret` from your Slack App's **Basic Information** page.
* **Never commit tokens.** All secrets are injected via environment variables.
* **Loop protection.** Guard clauses in `processAsync` ignore bot messages, edits, and system events to prevent the agent from correcting itself in an infinite loop.

---

## 🧱 Tech Stack

* **Java 21** · **Spring Boot 3.4** · **Spring AI 1.1** (`spring-ai-starter-model-google-genai`)
* **Slack Java SDK 1.39** (`com.slack.api:slack-api-client`)
* **Spring Retry**, **Spring WebFlux**, **Lombok**

---

*Type messy. Sound sharp.*
