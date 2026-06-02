package com.pcbuildstore.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
    private static final Gson GSON = new Gson();

    public static ChatConfig load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE);
                ChatConfig cfg = GSON.fromJson(json, ChatConfig.class);
                if (cfg != null) return cfg;
            }
        } catch (IOException ignored) {}
        return new ChatConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(CONFIG_FILE, GSON.toJson(this));
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
}
