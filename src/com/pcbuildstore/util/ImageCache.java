package com.pcbuildstore.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {

    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, ImageIcon> DEFAULTS = new ConcurrentHashMap<>();

    private ImageCache() {}

    public static ImageIcon get(String pathOrUrl, int categoryId, int w, int h) {
        String key = ((pathOrUrl == null) ? "" : pathOrUrl.trim()) + "|" + w + "x" + h;
        ImageIcon cached = CACHE.get(key);
        if (cached != null) return cached;

        return CACHE.computeIfAbsent(key, k -> {
            if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
                return getDefault(categoryId, w, h);
            }
            return loadFromUrl(pathOrUrl.trim(), categoryId, w, h);
        });
    }

    private static ImageIcon loadFromUrl(String urlStr, int categoryId, int w, int h) {
        try {
            URL url = java.net.URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 PCBuildStore/2.0");
            conn.setRequestProperty("Accept", "image/*,*/*;q=0.8");
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return getDefault(categoryId, w, h);
            }
            try (var in = conn.getInputStream()) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return getDefault(categoryId, w, h);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            return getDefault(categoryId, w, h);
        }
    }

    public static ImageIcon getDefault(int categoryId, int w, int h) {
        ImageIcon cached = DEFAULTS.get(categoryId);
        if (cached != null && cached.getIconWidth() == w && cached.getIconHeight() == h) {
            return cached;
        }

        Color bg = colorForCategory(categoryId);
        String label = labelForCategory(categoryId);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        int fontSize = Math.max(9, Math.min(w, h) / 4);
        g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        int th = fm.getHeight();
        g2.setColor(new Color(8, 8, 12));
        g2.drawString(label, (w - tw) / 2, (h - th) / 2 + fm.getAscent());

        g2.dispose();

        ImageIcon icon = new ImageIcon(img);
        DEFAULTS.put(categoryId, icon);
        return icon;
    }

    public static void invalidate(String pathOrUrl) {
        if (pathOrUrl == null) return;
        CACHE.entrySet().removeIf(e -> e.getKey().startsWith(pathOrUrl.trim() + "|"));
    }

    public static void clearAll() {
        CACHE.clear();
        DEFAULTS.clear();
    }

    private static Color colorForCategory(int categoryId) {
        switch (categoryId) {
            case 1:  return new Color(0, 113, 197);
            case 2:  return new Color(118, 185, 0);
            case 3:  return new Color(0, 200, 180);
            case 4:  return new Color(140, 60, 255);
            case 5:  return new Color(255, 140, 0);
            default: return new Color(80, 80, 100);
        }
    }

    private static String labelForCategory(int categoryId) {
        switch (categoryId) {
            case 1:  return "CPU";
            case 2:  return "GPU";
            case 3:  return "RAM";
            case 4:  return "SSD";
            case 5:  return "PSU";
            default: return "?";
        }
    }
}
