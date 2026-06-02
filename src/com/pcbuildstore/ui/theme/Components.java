package com.pcbuildstore.ui.theme;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

public final class Components {

    private Components() {}

    public static JPanel panel() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Theme.SURFACE);
        return p;
    }

    public static JPanel card() {
        JPanel c = new JPanel();
        c.setOpaque(true);
        c.setBackground(Theme.SURFACE);
        c.setBorder(new RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM));
        return c;
    }

    public static JPanel softCard() {
        JPanel c = new JPanel();
        c.setOpaque(true);
        c.setBackground(Theme.SURFACE_2);
        c.setBorder(new RoundedBorder(Theme.BORDER_SOFT, 1, Theme.R_MEDIUM));
        return c;
    }

    public static JPanel dividerH(int h) {
        JPanel d = new JPanel();
        d.setOpaque(true);
        d.setBackground(Theme.BORDER);
        d.setPreferredSize(new Dimension(0, h));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return d;
    }

    public static JPanel dividerV(int w) {
        JPanel d = new JPanel();
        d.setOpaque(true);
        d.setBackground(Theme.BORDER);
        d.setPreferredSize(new Dimension(w, 0));
        d.setMaximumSize(new Dimension(w, Integer.MAX_VALUE));
        return d;
    }

    public static JPanel vSpacer(int h) {
        JPanel s = new JPanel();
        s.setOpaque(false);
        s.setPreferredSize(new Dimension(0, h));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return s;
    }

    public static JPanel hSpacer(int w) {
        JPanel s = new JPanel();
        s.setOpaque(false);
        s.setPreferredSize(new Dimension(w, 0));
        s.setMaximumSize(new Dimension(w, Integer.MAX_VALUE));
        return s;
    }

    public static JLabel display(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.light(size));
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel heading(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.regular(size));
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel body(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.regular(size));
        l.setForeground(Theme.TEXT_2);
        return l;
    }

    public static JLabel caption(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.regular(size));
        l.setForeground(Theme.TEXT_3);
        return l;
    }

    public static JLabel eyebrow(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.medium(9));
        l.setForeground(Theme.TEXT_3);
        return l;
    }

    public static JLabel mono(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.mono(size));
        l.setForeground(color);
        return l;
    }

    public static JLabel price(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(size));
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel accentLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(size));
        l.setForeground(color);
        return l;
    }

    public static JLabel pillTag(String text, Color bg, Color fg) {
        JLabel l = new JLabel("  " + text.toUpperCase() + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 999, 999));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setFont(Theme.medium(8));
        l.setForeground(fg);
        l.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    public static JButton primaryButton(String text) {
        JButton b = makeBaseButton(text, Theme.TEXT_INV, Theme.medium(11));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { ((JButton) e.getSource()).putClientProperty("hover", true); ((JButton) e.getSource()).repaint(); }
            public void mouseExited(MouseEvent e)  { ((JButton) e.getSource()).putClientProperty("hover", false); ((JButton) e.getSource()).repaint(); }
        });
        b.putClientProperty("variant", "primary");
        b.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = makeBaseButton(text, Theme.TEXT, Theme.medium(11));
        b.putClientProperty("variant", "secondary");
        b.setBorder(BorderFactory.createEmptyBorder(11, 21, 11, 21));
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = makeBaseButton(text, Theme.TEXT, Theme.medium(11));
        b.putClientProperty("variant", "ghost");
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    public static JButton saleButton(String text) {
        JButton b = makeBaseButton(text, Theme.TEXT_INV, Theme.medium(11));
        b.putClientProperty("variant", "sale");
        b.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        return b;
    }

    private static JButton makeBaseButton(String text, Color fg, Font f) {
        JButton b = new JButton(text.toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String variant = (String) getClientProperty("variant");
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                Color bg;
                if ("primary".equals(variant)) {
                    bg = hover ? Theme.ACCENT_HOV : Theme.ACCENT;
                } else if ("secondary".equals(variant)) {
                    bg = hover ? Theme.SURFACE_3 : Theme.SURFACE_2;
                } else if ("ghost".equals(variant)) {
                    bg = hover ? Theme.SURFACE_2 : new Color(0, 0, 0, 0);
                } else if ("sale".equals(variant)) {
                    bg = hover ? new Color(0xD8, 0x3A, 0x18) : Theme.SALE;
                } else {
                    bg = Theme.SURFACE_2;
                }
                if (bg.getAlpha() > 0) {
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), Theme.R_PILL, Theme.R_PILL));
                }
                if ("secondary".equals(variant) || "ghost".equals(variant)) {
                    if (hover || "secondary".equals(variant)) {
                        g2.setColor(Theme.BORDER_HARD);
                        g2.setStroke(new BasicStroke(1));
                        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, Theme.R_PILL, Theme.R_PILL));
                    }
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(fg);
        b.setFont(f);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.putClientProperty("hover", true); b.repaint(); }
            public void mouseExited(MouseEvent e)  { b.putClientProperty("hover", false); b.repaint(); }
        });
        return b;
    }

    public static JLabel dot(Color c, int size) {
        JLabel p = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 0, size, size);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(size, size));
        return p;
    }

    public static JPanel heroPanel(Color c1, Color c2, int h) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(0, h));
        p.setLayout(new BorderLayout());
        return p;
    }

    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;
        public RoundedBorder(Color c, int t, int r) { this.color = c; this.thickness = t; this.radius = r; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                x + thickness / 2f, y + thickness / 2f,
                width - thickness, height - thickness,
                radius, radius
            ));
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
    }
}
