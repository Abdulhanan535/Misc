package com.pcbuildstore.util;

import com.pcbuildstore.models.Part;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ImageCache {

    private static final int MAX_MEMORY = 120;
    private static final Path CACHE_DIR = Path.of(System.getProperty("user.home"), ".pcbuildstore", "cache", "images");

    private static final LinkedHashMap<String, ImageIcon> MEMORY = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> eldest) {
            return size() > MAX_MEMORY;
        }
    };
    private static final Map<String, ImageIcon> DEFAULTS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> FAILED = new ConcurrentHashMap<>();

    private static final ExecutorService LOADER = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "img-loader");
        t.setDaemon(true);
        return t;
    });

    static {
        try { Files.createDirectories(CACHE_DIR); } catch (Exception ignored) {}
    }

    private ImageCache() {}

    public static ImageIcon get(String pathOrUrl, int categoryId, int w, int h) {
        String memKey = ((pathOrUrl == null) ? "" : pathOrUrl.trim()) + "|" + w + "x" + h;

        synchronized (MEMORY) {
            ImageIcon cached = MEMORY.get(memKey);
            if (cached != null) return cached;
        }

        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            return getDefault(categoryId, w, h);
        }

        String diskKey = sanitize(pathOrUrl.trim(), w, h);
        File diskFile = CACHE_DIR.resolve(diskKey).toFile();

        if (diskFile.exists() && diskFile.length() > 0) {
            try (FileInputStream fis = new FileInputStream(diskFile)) {
                BufferedImage img = ImageIO.read(fis);
                if (img != null) {
                    BufferedImage scaled = scale(img, w, h);
                    ImageIcon icon = new ImageIcon(scaled);
                    synchronized (MEMORY) { MEMORY.put(memKey, icon); }
                    return icon;
                }
            } catch (Exception ignored) {}
        }

        if (Boolean.TRUE.equals(FAILED.get(memKey))) return getDefault(categoryId, w, h);

        try {
            byte[] bytes = downloadBytes(pathOrUrl.trim());
            if (bytes != null) {
                BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                if (raw != null) {
                    BufferedImage scaled = scale(raw, w, h);
                    ImageIcon icon = new ImageIcon(scaled);
                    synchronized (MEMORY) { MEMORY.put(memKey, icon); }
                    saveBytes(diskFile, bytes);
                    return icon;
                }
            }
        } catch (Exception ignored) {}

        FAILED.put(memKey, true);
        return getDefault(categoryId, w, h);
    }

    private static byte[] downloadBytes(String urlStr) {
        try {
            URL url = java.net.URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 PCBuildStore/2.0");
            conn.setRequestProperty("Accept", "image/*,*/*;q=0.8");
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                return out.toByteArray();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return dst;
    }

    private static void saveBytes(File file, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (Exception ignored) {}
    }

    public static ImageIcon getDefault(int categoryId, int w, int h) {
        String key = categoryId + "|" + w + "x" + h;
        ImageIcon cached = DEFAULTS.get(key);
        if (cached != null) return cached;

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
        DEFAULTS.put(key, icon);
        return icon;
    }

    public static void loadBatch(List<Part> parts, Map<Integer, javax.swing.JLabel> labels,
                                  int w, int h, Runnable onDone) {
        List<Part> todo = new ArrayList<>();
        for (Part p : parts) {
            javax.swing.JLabel lbl = labels.get(p.getPartId());
            if (lbl != null) todo.add(p);
        }
        if (todo.isEmpty()) { if (onDone != null) Swing(onDone); return; }

        CountDownLatch latch = new CountDownLatch(todo.size());
        for (Part p : todo) {
            javax.swing.JLabel lbl = labels.get(p.getPartId());
            LOADER.submit(() -> {
                try {
                    ImageIcon icon = get(p.getImagePath(), p.getCategoryId(), w, h);
                    SwingUtilities_updateIcon(lbl, icon);
                } catch (Exception ignored) {}
                latch.countDown();
            });
        }
        if (onDone != null) {
            LOADER.submit(() -> {
                try { latch.await(); } catch (InterruptedException ignored) {}
                Swing(onDone);
            });
        }
    }

    private static void Swing(Runnable r) {
        javax.swing.SwingUtilities.invokeLater(r);
    }

    private static void SwingUtilities_updateIcon(javax.swing.JLabel lbl, ImageIcon icon) {
        javax.swing.SwingUtilities.invokeLater(() -> lbl.setIcon(icon));
    }

    public static void invalidate(String pathOrUrl) {
        if (pathOrUrl == null) return;
        String trimmed = pathOrUrl.trim();
        synchronized (MEMORY) {
            MEMORY.entrySet().removeIf(e -> e.getKey().startsWith(trimmed + "|"));
        }
        FAILED.remove(trimmed);
    }

    public static void clearAll() {
        synchronized (MEMORY) { MEMORY.clear(); }
        DEFAULTS.clear();
        FAILED.clear();
    }

    public static void shutdown() {
        LOADER.shutdown();
        try { LOADER.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    private static String sanitize(String url, int w, int h) {
        String base = url.replaceAll("[^a-zA-Z0-9]", "_");
        if (base.length() > 120) base = base.substring(0, 120);
        return base + "_" + w + "x" + h + ".png";
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
