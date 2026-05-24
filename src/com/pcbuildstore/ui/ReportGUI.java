package com.pcbuildstore.ui;

import com.pcbuildstore.dao.ReportDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ReportGUI extends JPanel {

    private static final Color BG = DashboardGUI.BG;
    private static final Color CARD = DashboardGUI.CARD;
    private static final Color BORDER = DashboardGUI.BORDER;
    private static final Color TEXT = DashboardGUI.TEXT;
    private static final Color MUTED = DashboardGUI.MUTED;
    private static final Color MINT = DashboardGUI.MINT;
    private static final Color BLUE = DashboardGUI.BLUE;
    private static final Color VIOLET = DashboardGUI.VIOLET;
    private static final Color EMBER = DashboardGUI.EMBER;
    private static final Color GOLD = DashboardGUI.GOLD;
    private static final Color ROSE = DashboardGUI.ROSE;

    private final ReportDAO reportDAO = new ReportDAO();

    public ReportGUI() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        loadData();
    }

    private void buildUI() {
        add(createHeader(), BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(BG);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        scrollContent.add(createUsagePanel());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 15)));
        scrollContent.add(createBrandPanel());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 15)));
        scrollContent.add(createPriceDistPanel());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 15)));
        scrollContent.add(createScoreDistPanel());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 15)));
        scrollContent.add(createBuildPartsPanel());
        scrollContent.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        header.add(DashboardGUI.label("Reports & Analytics", TEXT, 28, Font.BOLD));
        header.add(Box.createHorizontalGlue());

        return header;
    }

    private JPanel createUsagePanel() {
        JPanel panel = createCard();
        panel.add(DashboardGUI.label("Part Category Usage in Builds", TEXT, 14, Font.BOLD), BorderLayout.NORTH);

        Map<String, Integer> data = reportDAO.getBuildsPerCategory();
        if (data.isEmpty()) {
            panel.add(DashboardGUI.label("No build data available", MUTED, 12, Font.PLAIN), BorderLayout.CENTER);
            return panel;
        }

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.Y_AXIS));
        chart.setBackground(CARD);

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Color[] colors = {BLUE, new Color(118, 185, 0), MINT, VIOLET, EMBER};

        int idx = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            final int ci = idx;
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(CARD);
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel label = DashboardGUI.label(e.getKey(), TEXT, 11, Font.PLAIN);
            label.setPreferredSize(new Dimension(80, 20));
            row.add(label, BorderLayout.WEST);

            JPanel bar = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = (int) ((double) e.getValue() / max * (getWidth() - 10));
                    g2.setColor(colors[ci % colors.length]);
                    g2.fillRoundRect(0, 2, w, getHeight() - 4, 6, 6);
                }
            };
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(200, 20));
            row.add(bar, BorderLayout.CENTER);

            JLabel count = DashboardGUI.label(String.valueOf(e.getValue()), colors[ci % colors.length], 11, Font.BOLD);
            count.setPreferredSize(new Dimension(40, 20));
            count.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(count, BorderLayout.EAST);

            chart.add(row);
            idx++;
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBrandPanel() {
        JPanel panel = createCard();
        panel.add(DashboardGUI.label("Parts Usage by Brand", TEXT, 14, Font.BOLD), BorderLayout.NORTH);

        Map<String, Integer> data = reportDAO.getPartsUsageByBrand();
        if (data.isEmpty()) {
            panel.add(DashboardGUI.label("No data available", MUTED, 12, Font.PLAIN), BorderLayout.CENTER);
            return panel;
        }

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.X_AXIS));
        grid.setBackground(CARD);
        grid.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Color[] colors = {BLUE, MINT, VIOLET, EMBER, GOLD, ROSE};
        int idx = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            final int ci = idx;
            JPanel item = new JPanel(new BorderLayout());
            item.setBackground(CARD);
            item.setPreferredSize(new Dimension(120, 60));

            JLabel name = DashboardGUI.label(e.getKey(), TEXT, 11, Font.BOLD);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            item.add(name, BorderLayout.NORTH);

            int barH = (int) ((double) e.getValue() / max * 30) + 5;
            JPanel barWrap = new JPanel(new BorderLayout());
            barWrap.setOpaque(false);
            JPanel bar = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(colors[ci % colors.length]);
                    int cx = getWidth() / 2;
                    g2.fillRoundRect(cx - 15, getHeight() - barH, 30, barH, 6, 6);
                }
            };
            bar.setOpaque(false);
            barWrap.add(bar, BorderLayout.CENTER);
            item.add(barWrap, BorderLayout.CENTER);

            JLabel count = DashboardGUI.label(String.valueOf(e.getValue()), colors[ci % colors.length], 12, Font.BOLD);
            count.setHorizontalAlignment(SwingConstants.CENTER);
            item.add(count, BorderLayout.SOUTH);

            panel.add(item, BorderLayout.CENTER);
            if (ci < data.size() - 1) {
                JPanel sep = new JPanel();
                sep.setBackground(BORDER);
                sep.setPreferredSize(new Dimension(1, 60));
                panel.add(sep, BorderLayout.EAST);
            }
            idx++;
        }

        return panel;
    }

    private JPanel createPriceDistPanel() {
        JPanel panel = createCard();
        panel.add(DashboardGUI.label("Build Price Distribution", TEXT, 14, Font.BOLD), BorderLayout.NORTH);

        int[] data = reportDAO.getPriceDistribution();
        String[] labels = reportDAO.getPriceDistributionLabels();
        int max = 1;
        for (int v : data) if (v > max) max = v;

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.X_AXIS));
        chart.setBackground(CARD);
        chart.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Color[] colors = {MINT, BLUE, VIOLET, EMBER, GOLD};
        for (int i = 0; i < data.length; i++) {
            JPanel col = new JPanel(new BorderLayout());
            col.setBackground(CARD);
            col.setPreferredSize(new Dimension(80, 120));

            final int val = data[i];
            final int mx = max;
            final Color clr = colors[i];

            JPanel barArea = new JPanel(new BorderLayout()) {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = mx > 0 ? (int) ((double) val / mx * 70) : 0;
                    int cx = getWidth() / 2;
                    g2.setColor(clr);
                    g2.fillRoundRect(cx - 18, getHeight() - h - 20, 36, h, 6, 6);
                    g2.setColor(TEXT);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String txt = String.valueOf(val);
                    int tw = g2.getFontMetrics().stringWidth(txt);
                    g2.drawString(txt, cx - tw / 2, getHeight() - h - 25);
                }
            };
            barArea.setOpaque(false);
            col.add(barArea, BorderLayout.CENTER);

            JLabel lbl = DashboardGUI.label(labels[i], MUTED, 9, Font.PLAIN);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            col.add(lbl, BorderLayout.SOUTH);

            chart.add(col);
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createScoreDistPanel() {
        JPanel panel = createCard();
        panel.add(DashboardGUI.label("Build Score Distribution", TEXT, 14, Font.BOLD), BorderLayout.NORTH);

        int[] data = reportDAO.getScoreDistribution();
        String[] labels = reportDAO.getScoreDistributionLabels();
        int max = 1;
        for (int v : data) if (v > max) max = v;

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.X_AXIS));
        chart.setBackground(CARD);
        chart.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Color[] colors = {ROSE, EMBER, GOLD, MINT, BLUE};
        for (int i = 0; i < data.length; i++) {
            JPanel col = new JPanel(new BorderLayout());
            col.setBackground(CARD);
            col.setPreferredSize(new Dimension(80, 120));

            final int val = data[i];
            final int mx = max;
            final Color clr = colors[i];

            JPanel barArea = new JPanel(new BorderLayout()) {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = mx > 0 ? (int) ((double) val / mx * 70) : 0;
                    int cx = getWidth() / 2;
                    g2.setColor(clr);
                    g2.fillRoundRect(cx - 18, getHeight() - h - 20, 36, h, 6, 6);
                    g2.setColor(TEXT);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String txt = String.valueOf(val);
                    int tw = g2.getFontMetrics().stringWidth(txt);
                    g2.drawString(txt, cx - tw / 2, getHeight() - h - 25);
                }
            };
            barArea.setOpaque(false);
            col.add(barArea, BorderLayout.CENTER);

            JLabel lbl = DashboardGUI.label(labels[i], MUTED, 9, Font.PLAIN);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            col.add(lbl, BorderLayout.SOUTH);

            chart.add(col);
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBuildPartsPanel() {
        JPanel panel = createCard();
        panel.add(DashboardGUI.label("Build Parts Breakdown", TEXT, 14, Font.BOLD), BorderLayout.NORTH);

        String[] cols = {"Build", "Category", "Part", "Price (PKR)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleSmallTable(table);

        List<String[]> rows = reportDAO.getBuildPartDetails();
        for (String[] row : rows) {
            model.addRow(new Object[]{row[0], row[1], row[2], String.format("%,d", Integer.parseInt(row[3]))});
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        scroll.setPreferredSize(new Dimension(0, 200));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void styleSmallTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(BLUE.darker());
        table.setSelectionForeground(TEXT);
        table.setFont(DashboardGUI.font(Font.PLAIN, 11));
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 15, 22));
        header.setForeground(MUTED);
        header.setFont(DashboardGUI.font(Font.BOLD, 10));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 28));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setBackground(CARD);
        center.setForeground(TEXT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    private void loadData() {
    }
}
