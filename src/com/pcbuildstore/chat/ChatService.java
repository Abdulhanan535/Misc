package com.pcbuildstore.chat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChatService {

    public record Message(String role, String content) {}

    private final ChatConfig config;
    private final HttpClient client;
    private final Gson gson = new Gson();

    public ChatService(ChatConfig config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    }

    public void chatStream(List<Message> history, Consumer<String> onToken, Runnable onDone, Consumer<String> onError, AtomicBoolean cancel) {
        Thread t = new Thread(() -> doStream(history, onToken, onDone, onError, cancel), "chat-stream");
        t.setDaemon(true);
        t.start();
    }

    private void doStream(List<Message> history, Consumer<String> onToken, Runnable onDone, Consumer<String> onError, AtomicBoolean cancel) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", config.model);
            body.addProperty("stream", true);

            JsonArray msgs = new JsonArray();
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", config.systemPrompt);
            msgs.add(sys);
            for (Message m : history) {
                JsonObject jo = new JsonObject();
                jo.addProperty("role", m.role());
                jo.addProperty("content", m.content());
                msgs.add(jo);
            }
            body.add("messages", msgs);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.chatCompletionsUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            int code = resp.statusCode();
            if (code / 100 != 2) {
                String err = readAll(resp.body());
                onError.accept("HTTP " + code + ": " + truncate(err, 200));
                onDone.run();
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (cancel != null && cancel.get()) break;
                    if (line.isEmpty()) continue;
                    if (!line.startsWith("data:")) continue;
                    String payload = line.substring(5).trim();
                    if (payload.isEmpty() || "[DONE]".equals(payload)) continue;
                    try {
                        JsonObject ev = JsonParser.parseString(payload).getAsJsonObject();
                        JsonArray choices = ev.getAsJsonArray("choices");
                        if (choices == null || choices.isEmpty()) continue;
                        JsonObject choice = choices.get(0).getAsJsonObject();
                        if (choice.has("finish_reason") && !choice.get("finish_reason").isJsonNull()) {
                            String fr = choice.get("finish_reason").getAsString();
                            if ("stop".equals(fr) || "length".equals(fr) || "tool_calls".equals(fr)) continue;
                        }
                        JsonObject delta = choice.getAsJsonObject("delta");
                        if (delta == null) continue;
                        if (delta.has("content") && !delta.get("content").isJsonNull()) {
                            String tok = delta.get("content").getAsString();
                            if (!tok.isEmpty()) onToken.accept(tok);
                        }
                    } catch (Exception ignored) {}
                }
            }
            onDone.run();
        } catch (IOException ex) {
            onError.accept("Network error: " + ex.getMessage());
            onDone.run();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            onError.accept("Interrupted");
            onDone.run();
        } catch (Exception ex) {
            onError.accept("Error: " + ex.getMessage());
            onDone.run();
        }
    }

    private String readAll(InputStream is) throws IOException {
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "…";
    }
}
