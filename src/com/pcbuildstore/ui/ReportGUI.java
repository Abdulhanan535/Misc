package com.pcbuildstore.ui;

import com.pcbuildstore.dao.ReportDAO;
import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportGUI extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();

    public ReportGUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        buildUI();
    }

    public void onShow() { loadData(); }

    private void buildUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createScrollContent(), BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(Theme.BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(16, 36, 0, 36));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel ey = Components.eyebrow("ANALYTICS");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("Reports & insights");
        t.setFont(Theme.light(24));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(ey);
        left.add(Box.createVerticalStrut(2));
        left.add(t);
        row.add(left, BorderLayout.WEST);
        wrap.add(row);
        return wrap;
    }

    private JPanel createScrollContent() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(Theme.BG);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(14, 36, 24, 36));

        scrollContent.add(createUsagePanel());
        scrollContent.add(Components.vSpacer(12));
        scrollContent.add(createBrandPanel());
        scrollContent.add(Components.vSpacer(12));
        scrollContent.add(createPriceDistPanel());
        scrollContent.add(Components.vSpacer(12));
        scrollContent.add(createScoreDistPanel());
        scrollContent.add(Components.vSpacer(12));
        scrollContent.add(createBuildPartsPanel());
        scrollContent.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel card(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_LARGE));
        panel.setOpaque(true);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 0, 18));
        JLabel t = new JLabel(title);
        t.setFont(Theme.bold(13));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(t);
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createUsagePanel() {
        JPanel panel = card("Part category usage in builds");
        Map<String, Integer> data = reportDAO.getBuildsPerCategory();
        if (data.isEmpty()) {
            JLabel empty = new JLabel("No build data available");
            empty.setFont(Theme.regular(12));
            empty.setForeground(Theme.TEXT_3);
            empty.setBorder(BorderFactory.createEmptyBorder(0, 22, 22, 22));
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.Y_AXIS));
        chart.setOpaque(false);
        chart.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Color[] colors = {Theme.INTEL_BLUE, Theme.NVIDIA_GRN, Theme.INFO, Theme.HOT, Theme.AMD_RED};

        int idx = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            final int ci = idx;
            final int val = e.getValue();
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel nameLbl = new JLabel(e.getKey());
            nameLbl.setFont(Theme.medium(10));
            nameLbl.setForeground(Theme.TEXT);
            nameLbl.setPreferredSize(new Dimension(100, 20));
            row.add(nameLbl, BorderLayout.WEST);

            JPanel barTrack = new JPanel(new BorderLayout()) {
                private boolean painted = false;
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.SURFACE_2);
                    g2.fillRoundRect(0, 3, getWidth(), getHeight() - 6, 999, 999);
                    int w = (int) ((double) val / max * (getWidth() - 4));
                    g2.setColor(colors[ci % colors.length]);
                    g2.fillRoundRect(0, 3, w, getHeight() - 6, 999, 999);
                    g2.dispose();
                }
            };
            barTrack.setOpaque(false);
            barTrack.setPreferredSize(new Dimension(100, 14));
            row.add(barTrack, BorderLayout.CENTER);

            JLabel count = new JLabel(String.format("%,d", val));
            count.setFont(Theme.bold(11));
            count.setForeground(colors[ci % colors.length]);
            count.setPreferredSize(new Dimension(50, 20));
            count.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(count, BorderLayout.EAST);

            chart.add(row);
            idx++;
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBrandPanel() {
        JPanel panel = card("Parts usage by brand");
        Map<String, Integer> data = reportDAO.getPartsUsageByBrand();
        if (data.isEmpty()) {
            JLabel empty = new JLabel("No data available");
            empty.setFont(Theme.regular(12));
            empty.setForeground(Theme.TEXT_3);
            empty.setBorder(BorderFactory.createEmptyBorder(0, 22, 22, 22));
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.X_AXIS));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Color[] colors = {Theme.INTEL_BLUE, Theme.NVIDIA_GRN, Theme.INFO, Theme.HOT, Theme.AMD_RED, Theme.SALE};
        int idx = 0;
        int total = data.size();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            final int ci = idx;
            final int val = e.getValue();
            JPanel item = new JPanel(new BorderLayout(0, 4));
            item.setOpaque(false);
            item.setPreferredSize(new Dimension(120, 100));

            JLabel name = new JLabel(e.getKey());
            name.setFont(Theme.medium(10));
            name.setForeground(Theme.TEXT);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.add(name, BorderLayout.NORTH);

            JPanel barArea = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = (int) ((double) val / max * 40);
                    int cx = getWidth() / 2;
                    g2.setColor(Theme.SURFACE_2);
                    g2.fillRoundRect(cx - 14, 4, 28, getHeight() - 12, 999, 999);
                    g2.setColor(colors[ci % colors.length]);
                    g2.fillRoundRect(cx - 14, getHeight() - h - 10, 28, h, 999, 999);
                    g2.dispose();
                }
            };
            barArea.setOpaque(false);
            barArea.setPreferredSize(new Dimension(0, 50));
            item.add(barArea, BorderLayout.CENTER);

            JLabel count = new JLabel(String.format("%,d", val));
            count.setFont(Theme.bold(11));
            count.setForeground(colors[ci % colors.length]);
            count.setHorizontalAlignment(SwingConstants.CENTER);
            item.add(count, BorderLayout.SOUTH);

            grid.add(item);
            if (idx < total - 1) {
                JPanel sep = new JPanel();
                sep.setOpaque(true);
                sep.setBackground(Theme.BORDER_SOFT);
                sep.setPreferredSize(new Dimension(1, 64));
                sep.setMaximumSize(new Dimension(1, 64));
                grid.add(sep);
            }
            idx++;
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPriceDistPanel() {
        JPanel panel = card("Build price distribution");
        int[] data = reportDAO.getPriceDistribution();
        String[] labels = reportDAO.getPriceDistributionLabels();
        int max = 1;
        for (int v : data) if (v > max) max = v;

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.X_AXIS));
        chart.setOpaque(false);
        chart.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        Color[] colors = {Theme.NEW_TAG, Theme.INTEL_BLUE, Theme.HOT, Theme.AMD_RED, Theme.SALE};
        for (int i = 0; i < data.length; i++) {
            final int val = data[i];
            final int mx = max;
            final Color clr = colors[i];

            JPanel col = new JPanel(new BorderLayout(0, 6));
            col.setOpaque(false);
            col.setPreferredSize(new Dimension(80, 120));

            JPanel barArea = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = mx > 0 ? (int) ((double) val / mx * 60) : 0;
                    int cx = getWidth() / 2;
                    g2.setColor(Theme.SURFACE_2);
                    g2.fillRoundRect(cx - 14, 0, 28, getHeight() - 16, 8, 8);
                    g2.setColor(clr);
                    g2.fillRoundRect(cx - 14, getHeight() - h - 18, 28, h, 8, 8);

                    g2.setColor(Theme.TEXT);
                    g2.setFont(Theme.bold(10));
                    String txt = String.valueOf(val);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(txt);
                    g2.drawString(txt, cx - tw / 2, getHeight() - h - 22);
                    g2.dispose();
                }
            };
            barArea.setOpaque(false);
            barArea.setPreferredSize(new Dimension(0, 80));
            col.add(barArea, BorderLayout.CENTER);

            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Theme.medium(8));
            lbl.setForeground(Theme.TEXT_3);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            col.add(lbl, BorderLayout.SOUTH);

            chart.add(col);
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createScoreDistPanel() {
        JPanel panel = card("Build score distribution");
        int[] data = reportDAO.getScoreDistribution();
        String[] labels = reportDAO.getScoreDistributionLabels();
        int max = 1;
        for (int v : data) if (v > max) max = v;

        JPanel chart = new JPanel();
        chart.setLayout(new BoxLayout(chart, BoxLayout.X_AXIS));
        chart.setOpaque(false);
        chart.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        Color[] colors = {Theme.SALE, Theme.AMD_RED, Theme.WARN, Theme.NEW_TAG, Theme.INTEL_BLUE};
        for (int i = 0; i < data.length; i++) {
            final int val = data[i];
            final int mx = max;
            final Color clr = colors[i];

            JPanel col = new JPanel(new BorderLayout(0, 6));
            col.setOpaque(false);
            col.setPreferredSize(new Dimension(80, 120));

            JPanel barArea = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = mx > 0 ? (int) ((double) val / mx * 60) : 0;
                    int cx = getWidth() / 2;
                    g2.setColor(Theme.SURFACE_2);
                    g2.fillRoundRect(cx - 14, 0, 28, getHeight() - 16, 8, 8);
                    g2.setColor(clr);
                    g2.fillRoundRect(cx - 14, getHeight() - h - 18, 28, h, 8, 8);

                    g2.setColor(Theme.TEXT);
                    g2.setFont(Theme.bold(10));
                    String txt = String.valueOf(val);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(txt);
                    g2.drawString(txt, cx - tw / 2, getHeight() - h - 22);
                    g2.dispose();
                }
            };
            barArea.setOpaque(false);
            barArea.setPreferredSize(new Dimension(0, 80));
            col.add(barArea, BorderLayout.CENTER);

            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Theme.medium(8));
            lbl.setForeground(Theme.TEXT_3);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            col.add(lbl, BorderLayout.SOUTH);

            chart.add(col);
        }

        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBuildPartsPanel() {
        JPanel panel = card("Build parts breakdown");
        String[] cols = {"Build", "Category", "Part", "Price (PKR)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        List<String[]> rows = reportDAO.getBuildPartDetails();
        for (String[] row : rows) {
            model.addRow(new Object[]{row[0], row[1], row[2], String.format("%,d", Integer.parseInt(row[3]))});
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        scroll.getViewport().setBackground(Theme.SURFACE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.SURFACE);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER_SOFT);
        table.setSelectionBackground(Theme.SURFACE_2);
        table.setSelectionForeground(Theme.TEXT);
        table.setFont(Theme.regular(11));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.SURFACE);
        header.setForeground(Theme.TEXT_3);
        header.setFont(Theme.medium(9));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SOFT));
        header.setPreferredSize(new Dimension(0, 32));

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setBackground(sel ? Theme.SURFACE_2 : Theme.SURFACE);
                l.setForeground(Theme.TEXT);
                l.setFont(Theme.regular(11));
                l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (col == 3) { l.setHorizontalAlignment(SwingConstants.RIGHT); l.setFont(Theme.bold(11)); }
                if (col == 1) { l.setForeground(Theme.TEXT_3); }
                return l;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    private void loadData() {
        // re-create scroll to refresh numbers; cheap because data is small
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }
}
