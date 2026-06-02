package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BillDAO;
import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Bill;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import com.pcbuildstore.models.Category;
import com.pcbuildstore.models.Part;
import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;
import com.pcbuildstore.util.ImageCache;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardGUI extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final BuildDAO buildDAO = new BuildDAO();
    private final BillDAO billDAO = new BillDAO();
    private final PartDAO partDAO = new PartDAO();
    private final com.pcbuildstore.dao.CategoryDAO categoryDAO = new com.pcbuildstore.dao.CategoryDAO();

    private final Map<String, NavItem> navItems = new HashMap<>();
    private NavItem activeNav;

    private JLabel sBuilds, sRevenue, sAvg, sPurch, sHighest, sCatalog;
    private JPanel featuredRow;
    private JPanel recentBuildsRow;
    private JLabel liveClock;

    private final Map<Integer, Part> partById = new HashMap<>();
    private final Map<Integer, ImageIcon> preloadedIcons = new ConcurrentHashMap<>();
    private SwingWorker<Void, Integer> currentImageLoader;

    private BuildCatalogGUI buildCatalogGUI;
    private GPUUpgradesGUI gpuUpgradesGUI;
    private BillingGUI billingGUI;
    private ReportGUI reportGUI;

    private static final String[][] NAV = {
        {"DASH",    "Dashboard"},
        {"BUILDS",  "Build Configurator"},
        {"GPU",     "GPU Upgrades"},
        {"BILL",    "Billing"},
        {"REPORTS", "Reports"}
    };
    private static final String[] CAT_NAMES = {"Processors", "Graphics", "Memory", "Storage", "Power"};
    private static final int[]    CAT_IDS    = {1, 2, 3, 4, 5};
    private static final Color[]  CAT_ACC    = {Theme.INTEL_BLUE, Theme.NVIDIA_GRN, Theme.INFO, Theme.VIOLET, Theme.EMBER};

    public DashboardGUI() {
        setTitle("PC Build Store");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        getContentPane().setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(topNav(), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        content.setBackground(Theme.BG);

        content.add(dashView(), "DASH");

        buildCatalogGUI = new BuildCatalogGUI(this);
        gpuUpgradesGUI   = new GPUUpgradesGUI(this);
        billingGUI       = new BillingGUI(this);
        reportGUI        = new ReportGUI();

        content.add(buildCatalogGUI, "BUILDS");
        content.add(gpuUpgradesGUI,  "GPU");
        content.add(billingGUI,      "BILL");
        content.add(reportGUI,       "REPORTS");

        setContentPane(root);
        pack();
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setVisible(true);
        showView("DASH");
    }

    public void refreshStats() { loadStats(); loadFeaturedParts(); loadRecentBuildCards(); }

    public void showView(String id) {
        cards.show(content, id);
        if (activeNav != null) activeNav.setActive(false);
        NavItem ni = navItems.get(id);
        if (ni != null) { ni.setActive(true); activeNav = ni; }
        if (id.equals("DASH"))  refreshStats();
        if (id.equals("BUILDS")) buildCatalogGUI.onShow();
        if (id.equals("GPU"))    gpuUpgradesGUI.onShow();
        if (id.equals("BILL"))   billingGUI.onShow();
    }

    private JPanel topNav() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SIDEBAR);
        bar.setPreferredSize(new Dimension(0, 48));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel mark = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 0, 22, 22, 6, 6);
                g2.setColor(Theme.TEXT_INV);
                g2.setFont(Theme.bold(12));
                FontMetrics fm = g2.getFontMetrics();
                String s = "P";
                int tx = (22 - fm.stringWidth(s)) / 2;
                int ty = (22 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, tx, ty);
                g2.dispose();
            }
        };
        mark.setPreferredSize(new Dimension(22, 22));
        mark.setOpaque(false);
        left.add(mark);
        left.add(Box.createHorizontalStrut(8));
        JLabel brand = new JLabel("PC BUILD STORE");
        brand.setFont(Theme.medium(10));
        brand.setForeground(Theme.TEXT);
        left.add(brand);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 12));
        for (String[] n : NAV) {
            NavItem ni = new NavItem(n[0], n[1]);
            ni.addActionListener(e -> showView(n[0]));
            navItems.put(n[0], ni);
            right.add(ni);
        }
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel dashView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(Theme.BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Theme.BG);

        inner.add(hero());
        inner.add(createStatsRow());
        inner.add(Components.vSpacer(14));
        inner.add(createCategoryStrip());
        inner.add(Components.vSpacer(14));
        inner.add(sectionTitle("Featured parts", "Hand-picked from the catalog"));
        inner.add(Components.vSpacer(10));
        featuredRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        featuredRow.setOpaque(false);
        featuredRow.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));
        featuredRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(featuredRow);
        inner.add(Components.vSpacer(20));
        inner.add(sectionTitle("Recent builds", "Your last 5 saved builds"));
        inner.add(Components.vSpacer(10));
        recentBuildsRow = new JPanel();
        recentBuildsRow.setLayout(new BoxLayout(recentBuildsRow, BoxLayout.X_AXIS));
        recentBuildsRow.setOpaque(false);
        recentBuildsRow.setBorder(BorderFactory.createEmptyBorder(0, 36, 18, 36));
        recentBuildsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(recentBuildsRow);
        inner.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private JPanel hero() {
        JPanel hero = Components.heroPanel(
            new Color(0x10, 0x10, 0x18),
            new Color(0x06, 0x06, 0x0A),
            220
        );
        hero.setLayout(new BorderLayout());
        hero.setPreferredSize(new Dimension(0, 220));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 20));

        JLabel ey = Components.eyebrow(getGreeting().toUpperCase() + "  ·  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")).toUpperCase());
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(ey);
        left.add(Components.vSpacer(8));

        JLabel h1 = new JLabel("<html>Build the rig that defines you.</html>");
        h1.setFont(Theme.light(36));
        h1.setForeground(Theme.TEXT);
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(h1);
        left.add(Components.vSpacer(8));

        JLabel sub = new JLabel("<html><span style='color:#9090A8'>Curated parts. Compatibility-checked builds. Real performance scores — all in one place to design, save and purchase your next PC.</span></html>");
        sub.setFont(Theme.regular(12));
        sub.setForeground(Theme.TEXT_2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(sub);
        left.add(Components.vSpacer(14));

        JPanel cta = new JPanel();
        cta.setLayout(new BoxLayout(cta, BoxLayout.X_AXIS));
        cta.setOpaque(false);
        cta.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton start = Components.primaryButton("Start a new build");
        start.addActionListener(e -> showView("BUILDS"));
        cta.add(start);
        cta.add(Box.createHorizontalStrut(12));
        JButton browse = Components.secondaryButton("Browse parts");
        browse.addActionListener(e -> showView("BUILDS"));
        cta.add(browse);
        left.add(cta);

        hero.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0x00, 0xFF, 0xAA, 24));
                g2.fillOval(w - 280, 20, 240, 240);
                g2.setColor(new Color(0x8C, 0x3C, 0xFF, 28));
                g2.fillOval(w - 160, h - 160, 200, 200);
                g2.setColor(new Color(0xFF, 0x50, 0x28, 22));
                g2.fillOval(40, h - 130, 160, 160);

                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 12));
                g2.setStroke(new BasicStroke(1f));
                for (int i = 0; i < 6; i++) {
                    int r = 60 + i * 30;
                    g2.drawOval(w - r - 60, h/2 - r/2, r, r);
                }
                g2.dispose();
            }
        };
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(340, 0));
        right.setMinimumSize(new Dimension(260, 0));
        hero.add(right, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(hero, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createStatsRow() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(16, 36, 0, 36));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 1);
        gc.gridy = 0; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0; gc.weightx = 1;
        sBuilds  = makeStat("BUILDS",   "0",       Theme.ACCENT, row, gc);
        gc.gridx = 1; sRevenue = makeStat("REVENUE",      "0",        Theme.SUCCESS, row, gc);
        gc.gridx = 2; sAvg     = makeStat("AVG  SCORE",   "0",        Theme.VIOLET,  row, gc);
        gc.gridx = 3; sPurch   = makeStat("PURCHASES",    "0",        Theme.INFO,    row, gc);
        gc.gridx = 4; sHighest = makeStat("HIGHEST  SALE","0",        Theme.EMBER,   row, gc);
        gc.gridx = 5; gc.insets = new Insets(0, 0, 0, 0);
        sCatalog = makeStat("CATALOG",    "0",        Theme.GOLD, row, gc);
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel makeStat(String label, String value, Color accent, JPanel parent, GridBagConstraints gc) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, accent),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
        card.setOpaque(true);

        JLabel l = new JLabel(label);
        l.setFont(Theme.medium(8));
        l.setForeground(Theme.TEXT_3);
        JLabel v = new JLabel(value);
        v.setFont(Theme.mono(20));
        v.setForeground(Theme.TEXT);
        v.setName(label);

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        parent.add(card, gc);
        return v;
    }

    private JPanel createCategoryStrip() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < CAT_IDS.length; i++) {
            final int catId = CAT_IDS[i];
            final Color accent = CAT_ACC[i];
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
                private boolean hover = false;
                {
                    setOpaque(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                        public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                        public void mouseClicked(MouseEvent e) {
                            buildCatalogGUI.setCategory(catId);
                            showView("BUILDS");
                        }
                    });
                }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hover ? Theme.SURFACE_2 : Theme.SURFACE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_PILL, Theme.R_PILL);
                    g2.setColor(hover ? Theme.BORDER_HARD : Theme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.R_PILL, Theme.R_PILL);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            chip.setOpaque(false);
            chip.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 14));
            JLabel dot = Components.dot(accent, 8);
            dot.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
            chip.add(dot);
            JLabel t = new JLabel(CAT_NAMES[i]);
            t.setFont(Theme.medium(11));
            t.setForeground(Theme.TEXT);
            chip.add(t);
            JLabel ct = new JLabel(" · " + partDAO.getPartCountByCategory(catId));
            ct.setFont(Theme.regular(10));
            ct.setForeground(Theme.TEXT_3);
            chip.add(ct);
            row.add(chip);
        }
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel sectionTitle(String eyebrow, String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel e = Components.eyebrow(eyebrow.toUpperCase());
        e.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel(title);
        t.setFont(Theme.regular(15));
        t.setForeground(Theme.TEXT_2);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(e);
        p.add(Box.createVerticalStrut(2));
        p.add(t);
        return p;
    }

    private JPanel makeProductCard(Part p) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setPreferredSize(new Dimension(220, 240));
        card.setMinimumSize(new Dimension(220, 240));
        card.setMaximumSize(new Dimension(220, 240));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                buildCatalogGUI.setCategory(p.getCategoryId());
                showView("BUILDS");
            }
        });

        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setBackground(Theme.SURFACE_2);
        imgWrap.setPreferredSize(new Dimension(196, 116));
        JLabel imgLbl = new JLabel();
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setVerticalAlignment(SwingConstants.CENTER);
        imgWrap.add(imgLbl, BorderLayout.CENTER);

        ImageIcon icon = preloadedIcons.get(p.getPartId());
        if (icon == null) icon = ImageCache.getDefault(p.getCategoryId(), 96, 96);
        imgLbl.setIcon(icon);

        JLabel badge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 999, 999);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setText("  " + categoryName(p.getCategoryId()).toUpperCase() + "  ");
        badge.setFont(Theme.medium(8));
        badge.setForeground(Theme.TEXT);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        badge.setOpaque(false);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 0));
        badgeWrap.add(badge);
        imgWrap.add(badgeWrap, BorderLayout.NORTH);

        card.add(imgWrap, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        JLabel brand = new JLabel(p.getBrand().toUpperCase());
        brand.setFont(Theme.medium(9));
        brand.setForeground(Theme.TEXT_3);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(brand);
        JLabel name = new JLabel("<html><div style='width:180px'>" + p.getName() + "</div></html>");
        name.setFont(Theme.regular(12));
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(name);
        body.add(Box.createVerticalGlue());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel price = new JLabel("PKR " + String.format("%,d", p.getPrice()));
        price.setFont(Theme.bold(13));
        price.setForeground(Theme.ACCENT);
        footer.add(price, BorderLayout.WEST);

        JLabel score = new JLabel("★ " + p.getPerformanceScore());
        score.setFont(Theme.medium(10));
        score.setForeground(Theme.VIOLET);
        score.setHorizontalAlignment(SwingConstants.RIGHT);
        footer.add(score, BorderLayout.EAST);

        body.add(footer);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private String categoryName(int id) {
        switch (id) {
            case 1: return "CPU";
            case 2: return "GPU";
            case 3: return "RAM";
            case 4: return "Storage";
            case 5: return "PSU";
            default: return "?";
        }
    }

    private void loadFeaturedParts() {
        if (currentImageLoader != null && !currentImageLoader.isDone()) {
            currentImageLoader.cancel(true);
        }
        preloadedIcons.clear();
        partById.clear();

        featuredRow.removeAll();
        List<Part> all = new ArrayList<>();
        for (int cid : CAT_IDS) {
            List<Part> cat = partDAO.getPartsByCategory(cid);
            if (!cat.isEmpty()) all.add(cat.get(0));
        }

        for (Part p : all) {
            partById.put(p.getPartId(), p);
            featuredRow.add(makeProductCard(p));
        }
        featuredRow.revalidate();
        featuredRow.repaint();

        final List<Part> toLoad = new ArrayList<>();
        for (Part p : all) {
            if (p.hasImage()) toLoad.add(p);
        }
        if (toLoad.isEmpty()) return;
        currentImageLoader = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Part p : toLoad) {
                    if (isCancelled()) return null;
                    ImageIcon icon = ImageCache.get(p.getImagePath(), p.getCategoryId(), 96, 96);
                    if (isCancelled()) return null;
                    preloadedIcons.put(p.getPartId(), icon);
                    publish(p.getPartId());
                }
                return null;
            }
            @Override
            protected void process(List<Integer> chunks) {
                for (int partId : chunks) {
                    for (Component c : featuredRow.getComponents()) {
                        if (c instanceof JPanel) {
                            JLabel imgLbl = findImageLabel((JPanel) c);
                            if (imgLbl != null) {
                                for (Part p : partById.values()) {
                                    if (p.getPartId() == partId) {
                                        imgLbl.setIcon(preloadedIcons.get(partId));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        currentImageLoader.execute();
    }

    private JLabel findImageLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                if (p.getComponentCount() == 1 && p.getComponent(0) instanceof JLabel) {
                    JLabel l = (JLabel) p.getComponent(0);
                    if (l.getIcon() != null) return l;
                }
                JLabel deeper = findImageLabel(p);
                if (deeper != null) return deeper;
            }
        }
        return null;
    }

    private void loadRecentBuildCards() {
        recentBuildsRow.removeAll();
        List<Build> recent = buildDAO.getRecentBuilds(5);
        if (recent.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setPreferredSize(new Dimension(400, 100));
            JLabel t = new JLabel("No saved builds yet. Head to the builder to create your first.");
            t.setFont(Theme.regular(12));
            t.setForeground(Theme.TEXT_3);
            empty.add(t, BorderLayout.CENTER);
            recentBuildsRow.add(empty);
            recentBuildsRow.revalidate();
            return;
        }
        for (Build b : recent) {
            recentBuildsRow.add(makeBuildCard(b));
            recentBuildsRow.add(Box.createHorizontalStrut(14));
        }
        recentBuildsRow.revalidate();
        recentBuildsRow.repaint();
    }

    private JPanel makeBuildCard(Build b) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setPreferredSize(new Dimension(240, 150));
        card.setMinimumSize(new Dimension(240, 150));
        card.setMaximumSize(new Dimension(240, 150));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showView("BUILDS"); }
        });

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel name = new JLabel(b.getName());
        name.setFont(Theme.bold(13));
        name.setForeground(Theme.TEXT);
        head.add(name, BorderLayout.NORTH);
        String dateStr = b.getCreatedAt() != null
            ? b.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d  ·  HH:mm"))
            : "";
        JLabel date = new JLabel(dateStr);
        date.setFont(Theme.regular(9));
        date.setForeground(Theme.TEXT_3);
        head.add(date, BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);

        JPanel slots = new JPanel();
        slots.setLayout(new BoxLayout(slots, BoxLayout.Y_AXIS));
        slots.setOpaque(false);
        slots.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        List<BuildPart> parts = buildDAO.getBuildParts(b.getBuildId());
        String[] catNames = {"CPU", "GPU", "RAM", "SSD", "PSU"};
        Color[] catColors = {Theme.INTEL_BLUE, Theme.NVIDIA_GRN, Theme.INFO, Theme.VIOLET, Theme.EMBER};
        for (int i = 0; i < 5; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
            JLabel dot = Components.dot(catColors[i], 5);
            row.add(dot);
            JLabel cn = new JLabel(catNames[i]);
            cn.setFont(Theme.medium(8));
            cn.setForeground(Theme.TEXT_3);
            cn.setPreferredSize(new Dimension(30, 14));
            row.add(cn);
            String pname = "—";
            for (BuildPart bp : parts) {
                if (bp.getCategoryId() == i + 1) {
                    Part p = partDAO.getPartById(bp.getPartId());
                    if (p != null) { pname = truncate(p.getBrand() + " " + p.getName(), 18); break; }
                }
            }
            JLabel pn = new JLabel(pname);
            pn.setFont(Theme.regular(10));
            pn.setForeground(Theme.TEXT_2);
            row.add(pn);
            slots.add(row);
        }
        card.add(slots, BorderLayout.CENTER);

        JPanel totals = new JPanel(new BorderLayout());
        totals.setOpaque(false);
        JLabel price = new JLabel("PKR " + String.format("%,d", b.getTotalPrice()));
        price.setFont(Theme.mono(12));
        price.setForeground(Theme.ACCENT);
        totals.add(price, BorderLayout.WEST);
        JLabel score = new JLabel("★ " + b.getTotalScore());
        score.setFont(Theme.mono(11));
        score.setForeground(Theme.VIOLET);
        score.setHorizontalAlignment(SwingConstants.RIGHT);
        totals.add(score, BorderLayout.EAST);
        card.add(totals, BorderLayout.SOUTH);
        return card;
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    private void loadStats() {
        sBuilds.setText(String.format("%,d",  buildDAO.getTotalBuilds()));
        sRevenue.setText(String.format("%,d", billDAO.getTotalRevenue()));
        sAvg.setText(String.format("%.1f",   buildDAO.getAverageScore()));
        sPurch.setText(String.format("%,d",   billDAO.getTotalBills()));
        sHighest.setText(String.format("%,d", billDAO.getHighestBill()));
        sCatalog.setText(String.format("%,d", partDAO.getPartCount()));
    }

    public static JLabel label(String t, Color fg, int size, int style) {
        JLabel l = new JLabel(t);
        l.setForeground(fg);
        l.setFont(new Font("Segoe UI", style, size));
        return l;
    }

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    class NavItem extends JButton {
        private boolean on = false;
        private boolean hover = false;
        private final JLabel textLabel;
        private final JLabel bar;
        NavItem(String id, String text) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

            textLabel = new JLabel(text);
            textLabel.setFont(Theme.medium(10));
            textLabel.setForeground(Theme.TEXT_3);
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);

            int w = text.length() > 12 ? 130 : 100;
            setPreferredSize(new Dimension(w, 48));
            setMinimumSize(new Dimension(w, 48));

            bar = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (on) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Theme.ACCENT);
                        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                        g2.dispose();
                    }
                }
            };
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(0, 2));
            bar.setMinimumSize(new Dimension(0, 2));

            add(textLabel, BorderLayout.CENTER);
            add(bar, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; updateHover(); }
                public void mouseExited(MouseEvent e)  { hover = false; updateHover(); }
            });
        }
        void setActive(boolean active) {
            this.on = active;
            textLabel.setForeground(active ? Theme.TEXT : (hover ? Theme.TEXT_2 : Theme.TEXT_3));
            bar.repaint();
            repaint();
        }
        private void updateHover() {
            if (!on) {
                textLabel.setForeground(hover ? Theme.TEXT_2 : Theme.TEXT_3);
            }
        }
    }
}
