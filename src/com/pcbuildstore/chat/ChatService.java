package com.pcbuildstore.chat;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChatService {

    public record Message(String role, String content) {}

    private final ChatConfig config;
    private final HttpClient client;

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
            String body = buildRequestBody(history);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.chatCompletionsUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body))
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
                        String content = extractDeltaContent(payload);
                        if (content != null && !content.isEmpty()) onToken.accept(content);
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

    private String buildRequestBody(List<Message> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":").append(esc(config.model));
        sb.append(",\"stream\":true");
        sb.append(",\"messages\":[");
        sb.append("{\"role\":\"system\",\"content\":").append(esc(config.systemPrompt)).append("}");
        for (Message m : history) {
            sb.append(",{\"role\":").append(esc(m.role()));
            sb.append(",\"content\":").append(esc(m.content())).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String extractDeltaContent(String json) {
        String needle = "\"content\":\"";
        int idx = json.indexOf(needle);
        if (idx == -1) return null;
        idx += needle.length();
        StringBuilder sb = new StringBuilder();
        while (idx < json.length()) {
            char c = json.charAt(idx++);
            if (c == '\\' && idx < json.length()) {
                char next = json.charAt(idx++);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    default: sb.append('\\').append(next); break;
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String readAll(InputStream is) throws IOException {
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String esc(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(c); break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}
