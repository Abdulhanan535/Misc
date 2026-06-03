package com.pcbuildstore.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatConfig {
    public String baseUrl = "https://api.openai.com/v1";
    public String apiKey = "";
    public String model = "gpt-4o-mini";
    public String systemPrompt = "You are PC Assistant, a concise expert in PC building. Help users pick parts (CPU, GPU, RAM, storage, PSU), build budget rigs, explain compatibility, and compare prices. Prices are in PKR unless told otherwise. Keep replies under 80 words unless the user asks for detail. Use plain text, no markdown headings.";

    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".pcbuildstore");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    public static ChatConfig load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE);
                return parse(json);
            }
        } catch (IOException ignored) {}
        return new ChatConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(CONFIG_FILE, toJson());
        } catch (IOException ignored) {}
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String chatCompletionsUrl() {
        String b = baseUrl == null ? "" : baseUrl.trim();
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        if (b.endsWith("/v1")) return b + "/chat/completions";
        return b + "/v1/chat/completions";
    }

    private String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"baseUrl\":").append(esc(baseUrl));
        sb.append(",\"apiKey\":").append(esc(apiKey));
        sb.append(",\"model\":").append(esc(model));
        sb.append(",\"systemPrompt\":").append(esc(systemPrompt));
        sb.append("}");
        return sb.toString();
    }

    private static ChatConfig parse(String json) {
        ChatConfig cfg = new ChatConfig();
        cfg.baseUrl = extract(json, "baseUrl");
        cfg.apiKey = extract(json, "apiKey");
        cfg.model = extract(json, "model");
        cfg.systemPrompt = extract(json, "systemPrompt");
        if (cfg.baseUrl == null || cfg.baseUrl.isEmpty()) cfg.baseUrl = "https://api.openai.com/v1";
        if (cfg.model == null || cfg.model.isEmpty()) cfg.model = "gpt-4o-mini";
        if (cfg.systemPrompt == null) cfg.systemPrompt = "";
        return cfg;
    }

    private static String extract(String json, String key) {
        String needle = "\"" + key + "\":";
        int start = json.indexOf(needle);
        if (start == -1) return "";
        start += needle.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return "";
        if (json.charAt(start) == '"') {
            start++;
            StringBuilder sb = new StringBuilder();
            while (start < json.length()) {
                char c = json.charAt(start++);
                if (c == '\\' && start < json.length()) {
                    char next = json.charAt(start++);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
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
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) end = json.length();
        return json.substring(start, end).trim();
    }

    private static String esc(String s) {
        if (s == s) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c); break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
