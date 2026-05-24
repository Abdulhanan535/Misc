package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.BillDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.Bill;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DashboardGUI extends JFrame {

    static final Color BG = new Color(8, 8, 12);
    static final Color SIDEBAR = new Color(14, 14, 20);
    static final Color CARD = new Color(20, 20, 28);
    static final Color CARD_HOVER = new Color(28, 28, 38);
    static final Color BORDER = new Color(35, 35, 50);
    static final Color TEXT = new Color(245, 245, 250);
    static final Color MUTED = new Color(120, 120, 145);
    static final Color MINT = new Color(0, 255, 170);
    static final Color EMBER = new Color(255, 80, 40);
    static final Color VIOLET = new Color(140, 60, 255);
    static final Color ROSE = new Color(255, 50, 90);
    static final Color BLUE = new Color(50, 130, 255);
    static final Color GOLD = new Color(255, 200, 0);
    static final Color CYAN = new Color(0, 200, 255);

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final BuildDAO buildDAO = new BuildDAO();
    private final BillDAO billDAO = new BillDAO();
    private final PartDAO partDAO = new PartDAO();
    private NavItem activeNav;

    private JLabel s1, s2, s3, s4, s5, s6;
    private DefaultTableModel buildsModel;
    private DefaultTableModel billsModel;
    private JLabel cpuCount, gpuCount, ramCount, storageCount, psuCount;

    private BuildCatalogGUI buildCatalogGUI;
    private GPUUpgradesGUI gpuUpgradesGUI;
    private BillingGUI billingGUI;
    private ReportGUI reportGUI;

    public DashboardGUI() {
        setTitle("PC Build Store");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(sidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        content.add(dashView(), "DASH");

        buildCatalogGUI = new BuildCatalogGUI(this);
        gpuUpgradesGUI = new GPUUpgradesGUI(this);
        billingGUI = new BillingGUI(this);
        reportGUI = new ReportGUI();

        content.add(buildCatalogGUI, "BUILDS");
        content.add(gpuUpgradesGUI, "GPU");
        content.add(billingGUI, "BILL");
        content.add(reportGUI, "REPORTS");

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        showView("DASH");
    }

    public void refreshStats() {
        loadStats();
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        sidebar.add(logo());
        sidebar.add(spacer(20));
        sidebar.add(nav("DASH", "Dashboard", MINT));
        sidebar.add(nav("BUILDS", "Build Configurator", BLUE));
        sidebar.add(nav("GPU", "GPU Upgrades", VIOLET));
        sidebar.add(nav("BILL", "Billing", EMBER));
        sidebar.add(nav("REPORTS", "Reports", GOLD));
        sidebar.add(javax.swing.Box.createVerticalGlue());
        sidebar.add(footer());

        return sidebar;
    }

    private JPanel logo() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(SIDEBAR);
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel top = label("PC BUILD", MINT, 20, Font.BOLD);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel bot = label("STORE", TEXT, 14, Font.PLAIN);
        bot.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrap.add(top);
        wrap.add(bot);
        return wrap;
    }

    private NavItem nav(String id, String text, Color c) {
        NavItem item = new NavItem(id, text, c);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showView(id);
            }
        });
        return item;
    }

    private JPanel footer() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(SIDEBAR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel ver = label("v2.0 | PC Build Store", MUTED, 10, Font.PLAIN);
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(ver);
        return footer;
    }

    private JPanel dashView() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBackground(BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        inner.add(createWelcomeHeader());
        inner.add(spacer(20));
        inner.add(createStatsRow());
        inner.add(spacer(25));
        inner.add(createMiddleRow());
        inner.add(spacer(25));
        inner.add(createBottomRow());
        inner.add(javax.swing.Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private JPanel createWelcomeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        String greeting = getGreeting();
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);

        JLabel greet = label(greeting, TEXT, 26, Font.BOLD);
        greet.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(greet);

        JLabel date = label(dateStr, MUTED, 13, Font.PLAIN);
        date.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(date);

        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG);
        right.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        int totalParts = partDAO.getPartCount();
        int totalBuilds = buildDAO.getTotalBuilds();
        int totalBills = billDAO.getTotalBills();

        JLabel summary = label(totalBuilds + " builds  |  " + totalParts + " parts cataloged  |  " + totalBills + " purchases", MUTED, 11, Font.PLAIN);
        summary.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(summary);

        header.add(right, BorderLayout.EAST);

        return header;
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 12);
        gc.gridy = 0;
        gc.weighty = 1;

        gc.gridx = 0; gc.weightx = 1;
        s1 = addStatCard(row, gc, "Total Builds", "0", BLUE);
        gc.gridx = 1;
        s2 = addStatCard(row, gc, "Revenue", "PKR 0", MINT);
        gc.gridx = 2;
        s3 = addStatCard(row, gc, "Avg Score", "0", VIOLET);
        gc.gridx = 3;
        s4 = addStatCard(row, gc, "Purchases", "0", EMBER);
        gc.gridx = 4; gc.insets = new Insets(0, 0, 0, 0);
        s5 = addStatCard(row, gc, "Highest Sale", "PKR 0", GOLD);
        gc.gridx = 5;
        s6 = addStatCard(row, gc, "Parts Catalog", "0", CYAN);

        return row;
    }

    private JLabel addStatCard(JPanel parent, GridBagConstraints gc, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setPreferredSize(new Dimension(160, 85));
        card.setMinimumSize(new Dimension(140, 85));

        JLabel t = label(title, MUTED, 10, Font.PLAIN);
        JLabel v = label(value, accent, 18, Font.BOLD);

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);

        parent.add(card, gc);
        return v;
    }

    private JPanel createMiddleRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        row.add(createRecentBuildsPanel(), BorderLayout.CENTER);
        row.add(Box.createRigidArea(new Dimension(15, 0)), BorderLayout.CENTER);
        row.add(createPartsBreakdownPanel(), BorderLayout.EAST);

        return row;
    }

    private JPanel createRecentBuildsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = label("Recent Builds", TEXT, 14, Font.BOLD);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Name", "Price (PKR)", "Score", "Date"};
        buildsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(buildsModel);
        styleSmallTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPartsBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel title = label("Parts Catalog", TEXT, 14, Font.BOLD);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD);

        cpuCount = addPartRow(list, "CPUs", 1, BLUE);
        gpuCount = addPartRow(list, "GPUs", 2, NVIDIA);
        ramCount = addPartRow(list, "RAM", 3, MINT);
        storageCount = addPartRow(list, "Storage", 4, VIOLET);
        psuCount = addPartRow(list, "PSUs", 5, EMBER);

        panel.add(list, BorderLayout.CENTER);

        return panel;
    }

    private JLabel addPartRow(JPanel parent, String name, int catId, Color accent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel nameLabel = label(name, TEXT, 12, Font.PLAIN);
        row.add(nameLabel, BorderLayout.WEST);

        JLabel countLabel = label("0", accent, 12, Font.BOLD);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(countLabel, BorderLayout.EAST);

        parent.add(row);

        JPanel divider = new JPanel();
        divider.setBackground(BORDER);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        parent.add(divider);

        return countLabel;
    }

    private JPanel createBottomRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        row.add(createRecentBillsPanel(), BorderLayout.CENTER);
        row.add(Box.createRigidArea(new Dimension(15, 0)), BorderLayout.CENTER);
        row.add(createQuickActionsPanel(), BorderLayout.EAST);

        return row;
    }

    private JPanel createRecentBillsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = label("Recent Purchases", TEXT, 14, Font.BOLD);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Build", "Amount (PKR)", "Score", "Date"};
        billsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(billsModel);
        styleSmallTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel title = label("Quick Actions", TEXT, 14, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title);

        panel.add(actionButton("New Build", BLUE, "BUILDS"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(actionButton("GPU Upgrades", VIOLET, "GPU"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(actionButton("Purchase Build", EMBER, "BILL"));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel actionButton(String text, Color accent, String target) {
        JPanel btn = new JPanel(new BorderLayout()) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { showView(target); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? accent.darker() : accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
        };
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(190, 34));
        btn.setMaximumSize(new Dimension(190, 34));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel lbl = label(text, new Color(8, 8, 12), 12, Font.BOLD);
        btn.add(lbl, BorderLayout.CENTER);

        return btn;
    }

    private static final Color NVIDIA = new Color(118, 185, 0);

    private void showView(String id) {
        cards.show(content, id);
        if (activeNav != null) activeNav.setActive(false);
        for (Component c : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if (c instanceof NavItem && ((NavItem) c).id.equals(id)) {
                activeNav = (NavItem) c;
                activeNav.setActive(true);
            }
        }
        if (id.equals("DASH")) loadStats();
    }

    private void loadStats() {
        SwingUtilities.invokeLater(() -> {
            s1.setText(String.valueOf(buildDAO.getTotalBuilds()));
            s2.setText(String.format("PKR %,d", billDAO.getTotalRevenue()));
            s3.setText(String.format("%.1f", buildDAO.getAverageScore()));
            s4.setText(String.valueOf(billDAO.getTotalBills()));
            s5.setText(String.format("PKR %,d", billDAO.getHighestBill()));
            s6.setText(String.valueOf(partDAO.getPartCount()));

            cpuCount.setText(String.valueOf(partDAO.getPartCountByCategory(1)));
            gpuCount.setText(String.valueOf(partDAO.getPartCountByCategory(2)));
            ramCount.setText(String.valueOf(partDAO.getPartCountByCategory(3)));
            storageCount.setText(String.valueOf(partDAO.getPartCountByCategory(4)));
            psuCount.setText(String.valueOf(partDAO.getPartCountByCategory(5)));

            buildsModel.setRowCount(0);
            List<Build> recentBuilds = buildDAO.getRecentBuilds(5);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            for (Build b : recentBuilds) {
                buildsModel.addRow(new Object[]{
                    b.getName(),
                    String.format("%,d", b.getTotalPrice()),
                    b.getTotalScore(),
                    b.getCreatedAt() != null ? b.getCreatedAt().format(fmt) : ""
                });
            }

            billsModel.setRowCount(0);
            List<Bill> recentBills = billDAO.getRecentBills(5);
            for (Bill b : recentBills) {
                billsModel.addRow(new Object[]{
                    b.getBuildName(),
                    String.format("%,d", b.getFinalPrice()),
                    b.getFinalScore(),
                    b.getPurchaseDate() != null ? b.getPurchaseDate().format(fmt) : ""
                });
            }
        });
    }

    static JPanel panel(Color bg) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(bg);
        return p;
    }

    static JPanel card(Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        return p;
    }

    static JLabel label(String t, Color fg, int size, int style) {
        JLabel l = new JLabel(t);
        l.setForeground(fg);
        l.setFont(new Font("Segoe UI", style, size));
        return l;
    }

    static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    static Dimension dim(int w, int h) {
        return new Dimension(w, h);
    }

    static JPanel spacer(int h) {
        JPanel s = new JPanel();
        s.setBackground(null);
        s.setPreferredSize(new Dimension(0, h));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return s;
    }

    private void styleSmallTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(BLUE.darker());
        table.setSelectionForeground(TEXT);
        table.setFont(font(Font.PLAIN, 11));
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 15, 22));
        header.setForeground(MUTED);
        header.setFont(font(Font.BOLD, 10));
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

    class NavItem extends JPanel {
        final String id;
        private final Color accent;
        private boolean on = false;
        private final JLabel textLabel;

        NavItem(String id, String text, Color accent) {
            this.id = id;
            this.accent = accent;
            setLayout(new BorderLayout());
            setBackground(SIDEBAR);
            setPreferredSize(new Dimension(220, 42));
            setMaximumSize(new Dimension(220, 42));
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            textLabel = new JLabel(text);
            textLabel.setForeground(MUTED);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!on) textLabel.setForeground(TEXT);
                }
                public void mouseExited(MouseEvent e) {
                    if (!on) textLabel.setForeground(MUTED);
                }
            });
        }

        void setActive(boolean active) {
            this.on = active;
            textLabel.setForeground(active ? accent : MUTED);
            textLabel.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (on) {
                g.setColor(accent);
                g.fillRect(0, 0, 3, getHeight());
            }
        }
    }
}
