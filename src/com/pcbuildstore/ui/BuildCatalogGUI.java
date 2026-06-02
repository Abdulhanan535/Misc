package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.CategoryDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import com.pcbuildstore.models.Category;
import com.pcbuildstore.models.Part;
import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;
import com.pcbuildstore.util.ImageCache;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuildCatalogGUI extends JPanel {

    private final DashboardGUI dashboard;
    private final PartDAO partDAO = new PartDAO();
    private final BuildDAO buildDAO = new BuildDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private List<Category> categories;
    private int selectedCategoryId = 1;
    private int selectedPartId = -1;
    private int selectedBuildId = -1;
    private String selectedSocket = null;
    private String selectedDdrGen = null;

    private JPanel productGrid;
    private JTextField searchField;
    private JLabel countLabel;

    private JLabel totalPriceLabel;
    private JLabel totalScoreLabel;
    private final Map<Integer, JLabel> partSlotLabels = new HashMap<>();
    private final Map<Integer, JLabel> slotIconLabels = new HashMap<>();
    private final Map<Integer, Integer> currentBuildParts = new HashMap<>();
    private final Map<Integer, Part> partById = new HashMap<>();
    private final Map<Integer, ImageIcon> preloadedIcons = new ConcurrentHashMap<>();
    private SwingWorker<Void, Integer> currentImageLoader;

    private final List<JPanel> categoryTabs = new ArrayList<>();
    private JPanel buildsStrip;

    public BuildCatalogGUI(DashboardGUI dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        categories = categoryDAO.getAllCategories();
        buildUI();
        loadParts();
        loadBuilds();
    }

    public void onShow() { loadParts(); loadBuilds(); }

    public void setCategory(int catId) {
        selectedCategoryId = catId;
        for (JPanel tab : categoryTabs) {
            int id = (int) tab.getClientProperty("catId");
            tab.putClientProperty("active", id == catId);
            tab.repaint();
        }
        loadParts();
    }

    private void buildUI() {
        add(createTopBar(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.SIDEBAR);
        center.add(createCategoryTabs(), BorderLayout.NORTH);
        center.add(createProductArea(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(Theme.SIDEBAR);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(14, 36, 10, 36)
        ));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Build Configurator");
        title.setFont(Theme.light(22));
        title.setForeground(Theme.TEXT);
        left.add(title);
        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(Theme.SURFACE_2);
        searchWrap.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, 4));
        searchWrap.setPreferredSize(new Dimension(220, 32));

        JLabel sIcon = new JLabel("  ⌕ ");
        sIcon.setFont(Theme.regular(12));
        sIcon.setForeground(Theme.TEXT_3);
        searchWrap.add(sIcon, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setFont(Theme.regular(11));
        searchField.setForeground(Theme.TEXT);
        searchField.setCaretColor(Theme.TEXT);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 12));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterParts(); }
            public void removeUpdate(DocumentEvent e) { filterParts(); }
            public void changedUpdate(DocumentEvent e) { filterParts(); }
        });
        searchWrap.add(searchField, BorderLayout.CENTER);
        right.add(searchWrap);
        right.add(Box.createHorizontalStrut(10));

        JButton imageBtn = Components.secondaryButton("Set image");
        imageBtn.addActionListener(e -> openImageEditor());
        right.add(imageBtn);
        row.add(right, BorderLayout.EAST);
        wrap.add(row);
        return wrap;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.SIDEBAR);
        center.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return center;
    }

    private JPanel createCategoryTabs() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBackground(Theme.SIDEBAR);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 36, 14, 36));

        for (Category cat : categories) {
            final int catId = cat.getCategoryId();
            JPanel tab = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                    g2.setColor(active ? Theme.ACCENT : Theme.SURFACE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    if (!active) {
                        g2.setColor(Theme.BORDER);
                        g2.setStroke(new BasicStroke(1));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                    }
                    g2.dispose();
                }
            };
            tab.setOpaque(false);
            tab.setPreferredSize(new Dimension(110, 36));
            tab.setMinimumSize(new Dimension(110, 36));
            tab.setMaximumSize(new Dimension(110, 36));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.putClientProperty("catId", catId);
            tab.putClientProperty("active", catId == selectedCategoryId);
            JLabel lbl = new JLabel(cat.getName());
            lbl.setFont(Theme.medium(11));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setVerticalAlignment(SwingConstants.CENTER);
            tab.add(lbl);
            tab.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedCategoryId = catId;
                    selectedPartId = -1;
                    for (JPanel t : categoryTabs) {
                        t.putClientProperty("active", (Integer) t.getClientProperty("catId") == selectedCategoryId);
                        t.repaint();
                    }
                    loadParts();
                }
            });
            categoryTabs.add(tab);
            bar.add(tab);
        }
        return bar;
    }

    private JPanel createProductArea() {
        JPanel area = new JPanel(new BorderLayout(0, 0));
        area.setBackground(Theme.SIDEBAR);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setBackground(Theme.SIDEBAR);

        countLabel = new JLabel("0 parts");
        countLabel.setFont(Theme.regular(11));
        countLabel.setForeground(Theme.TEXT_3);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 36, 8, 0));
        leftPanel.add(countLabel, BorderLayout.NORTH);

        productGrid = new JPanel();
        productGrid.setLayout(new GridLayout(0, 3, 12, 12));
        productGrid.setBackground(Theme.SIDEBAR);
        productGrid.setBorder(BorderFactory.createEmptyBorder(0, 36, 8, 0));

        JPanel gridWrap = new JPanel();
        gridWrap.setLayout(new BoxLayout(gridWrap, BoxLayout.Y_AXIS));
        gridWrap.setBackground(Theme.SIDEBAR);
        gridWrap.add(productGrid);
        gridWrap.add(Box.createVerticalStrut(8));
        gridWrap.add(createBuildsStrip());
        gridWrap.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(gridWrap);
        Components.applyDarkScrollbar(scroll);
        leftPanel.add(scroll, BorderLayout.CENTER);
        area.add(leftPanel, BorderLayout.CENTER);

        area.add(createRightPanel(), BorderLayout.EAST);
        return area;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(Theme.SURFACE);
        right.setPreferredSize(new Dimension(260, 0));
        right.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER));
        right.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel ey = Components.eyebrow("YOUR  BUILD");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(ey);
        right.add(Components.vSpacer(4));
        JLabel title = new JLabel("Current build");
        title.setFont(Theme.bold(13));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(title);
        right.add(Components.vSpacer(10));

        String[] slotNames = {"Processor", "Graphics", "Memory", "Storage", "Power"};
        int[] slotIds = {1, 2, 3, 4, 5};
        Color[] slotColors = {Theme.INTEL_BLUE, Theme.NVIDIA_GRN, Theme.INFO, Theme.VIOLET, Theme.EMBER};

        for (int i = 0; i < slotNames.length; i++) {
            right.add(createSlot(slotNames[i], slotIds[i], slotColors[i]));
            right.add(Components.vSpacer(4));
        }

        right.add(Components.vSpacer(6));
        right.add(createTotalsCard());
        right.add(Components.vSpacer(8));

        JButton addBtn = Components.primaryButton("Add selected part");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addBtn.setFont(Theme.medium(10));
        addBtn.addActionListener(e -> addPartToBuild());
        right.add(addBtn);
        right.add(Components.vSpacer(6));

        JButton saveBtn = Components.saleButton("Save build");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        saveBtn.setFont(Theme.medium(10));
        saveBtn.addActionListener(e -> saveBuild());
        right.add(saveBtn);
        right.add(Components.vSpacer(6));

        JButton clearBtn = Components.ghostButton("Clear");
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        clearBtn.setFont(Theme.medium(10));
        clearBtn.addActionListener(e -> clearBuild());
        right.add(clearBtn);
        right.add(Box.createVerticalGlue());
        return right;
    }

    private JPanel createSlot(String name, int catId, Color accent) {
        JPanel slot = new JPanel(new BorderLayout(8, 0));
        slot.setBackground(Theme.SURFACE_2);
        slot.setBorder(new Components.RoundedBorder(Theme.BORDER_SOFT, 1, Theme.R_SMALL));
        slot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel dot = Components.dot(accent, 7);
        dot.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        slot.add(dot, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        JLabel cat = new JLabel(name.toUpperCase());
        cat.setFont(Theme.medium(8));
        cat.setForeground(Theme.TEXT_3);
        cat.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(cat);
        JLabel part = new JLabel("Not selected");
        part.setFont(Theme.regular(10));
        part.setForeground(Theme.TEXT_3);
        part.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(part);
        partSlotLabels.put(catId, part);
        slot.add(textCol, BorderLayout.CENTER);

        JLabel check = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String text = getText();
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if ("+".equals(text)) {
                    g2.setColor(getForeground());
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.drawLine(cx - 4, cy, cx + 4, cy);
                    g2.drawLine(cx, cy - 4, cx, cy + 4);
                } else if ("*".equals(text)) {
                    g2.setColor(getForeground());
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.drawLine(cx - 5, cy, cx + 5, cy);
                    g2.drawLine(cx, cy - 5, cx, cy + 5);
                    g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                    g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
                } else {
                    g2.setColor(getForeground());
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx - 5, cy + 5, cx + 5, cy - 5);
                }
                g2.dispose();
            }
        };
        check.setText("+");
        check.setFont(Theme.light(16));
        check.setForeground(Theme.TEXT_3);
        check.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        check.setPreferredSize(new Dimension(18, 18));
        check.setName("slot-icon-" + catId);
        slotIconLabels.put(catId, check);
        slot.add(check, BorderLayout.EAST);
        return slot;
    }

    private JPanel createTotalsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        JLabel pl = new JLabel("TOTAL");
        pl.setFont(Theme.medium(8));
        pl.setForeground(Theme.TEXT_3);
        totalPriceLabel = new JLabel("PKR 0");
        totalPriceLabel.setFont(Theme.mono(16));
        totalPriceLabel.setForeground(Theme.ACCENT);
        priceRow.add(pl, BorderLayout.NORTH);
        priceRow.add(totalPriceLabel, BorderLayout.CENTER);

        JPanel scoreRow = new JPanel(new BorderLayout());
        scoreRow.setOpaque(false);
        JLabel sl = new JLabel("SCORE");
        sl.setFont(Theme.medium(8));
        sl.setForeground(Theme.TEXT_3);
        totalScoreLabel = new JLabel("0");
        totalScoreLabel.setFont(Theme.mono(13));
        totalScoreLabel.setForeground(Theme.VIOLET);
        scoreRow.add(sl, BorderLayout.NORTH);
        scoreRow.add(totalScoreLabel, BorderLayout.CENTER);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        rows.add(priceRow);
        rows.add(Box.createVerticalStrut(2));
        rows.add(scoreRow);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBuildsStrip() {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(Theme.SURFACE);
        strip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 36, 10, 36)
        ));
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Saved builds");
        t.setFont(Theme.medium(10));
        t.setForeground(Theme.TEXT_3);
        header.add(t, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton loadBtn = Components.ghostButton("Load");
        loadBtn.setFont(Theme.medium(9));
        loadBtn.addActionListener(e -> loadSelectedBuild());
        JButton delBtn = Components.ghostButton("Delete");
        delBtn.setFont(Theme.medium(9));
        delBtn.setForeground(Theme.SALE);
        delBtn.addActionListener(e -> deleteSelectedBuild());
        right.add(loadBtn);
        right.add(delBtn);
        header.add(right, BorderLayout.EAST);
        strip.add(header, BorderLayout.NORTH);

        buildsStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buildsStrip.setOpaque(false);
        strip.add(buildsStrip, BorderLayout.CENTER);
        return strip;
    }

    private void loadParts() {
        productGrid.removeAll();
        partById.clear();
        List<Part> parts;
        if (selectedCategoryId == 3 && selectedDdrGen != null) {
            parts = partDAO.getCompatibleParts(selectedSocket, selectedDdrGen);
        } else {
            parts = partDAO.getPartsByCategory(selectedCategoryId);
        }

        for (Part p : parts) {
            partById.put(p.getPartId(), p);
            productGrid.add(makeProductCard(p));
        }
        countLabel.setText(parts.size() + " parts");
        productGrid.revalidate();
        productGrid.repaint();
        loadImagesAsync();
    }

    private void filterParts() {
        String query = searchField.getText().trim().toLowerCase();
        productGrid.removeAll();
        partById.clear();
        List<Part> parts;
        if (selectedCategoryId == 3 && selectedDdrGen != null) {
            parts = partDAO.getCompatibleParts(selectedSocket, selectedDdrGen);
        } else {
            parts = partDAO.getPartsByCategory(selectedCategoryId);
        }
        for (Part p : parts) {
            if (query.isEmpty() || p.getName().toLowerCase().contains(query)
                    || p.getBrand().toLowerCase().contains(query)) {
                partById.put(p.getPartId(), p);
                productGrid.add(makeProductCard(p));
            }
        }
        countLabel.setText(partById.size() + " parts");
        productGrid.revalidate();
        productGrid.repaint();
        loadImagesAsync();
    }

    private JPanel makeProductCard(Part p) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) {
                        selectedPartId = p.getPartId();
                        for (Component c : productGrid.getComponents()) {
                            if (c instanceof JPanel) c.repaint();
                        }
                    }
                    public void mouseDoubleClicked(MouseEvent e) { addPartToBuild(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? Theme.SURFACE_3 : Theme.SURFACE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_MEDIUM, Theme.R_MEDIUM);
                if (selectedPartId == p.getPartId()) {
                    g2.setColor(Theme.ACCENT);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, Theme.R_MEDIUM, Theme.R_MEDIUM);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setOpaque(false);
        imgWrap.setPreferredSize(new Dimension(0, 120));
        JLabel imgLbl = new JLabel();
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setVerticalAlignment(SwingConstants.CENTER);
        imgWrap.add(imgLbl, BorderLayout.CENTER);
        card.add(imgWrap, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel(p.getBrand().toUpperCase());
        brand.setFont(Theme.medium(8));
        brand.setForeground(Theme.TEXT_3);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(brand);

        JLabel name = new JLabel("<html><div style='width:180px;font-size:13px'>" + p.getName() + "</div></html>");
        name.setFont(Theme.regular(13));
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(name);
        body.add(Box.createVerticalGlue());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel price = new JLabel("PKR " + String.format("%,d", p.getPrice()));
        price.setFont(Theme.mono(12));
        price.setForeground(Theme.ACCENT);
        footer.add(price, BorderLayout.WEST);
        JLabel score = new JLabel(p.getPerformanceScore() + " pts");
        score.setFont(Theme.medium(10));
        score.setForeground(Theme.VIOLET);
        footer.add(score, BorderLayout.EAST);
        body.add(footer);

        card.add(body, BorderLayout.CENTER);

        JButton addBtn = new JButton("ADD") {
            private boolean h = false;
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setFont(Theme.medium(10));
                setForeground(Theme.ACCENT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { h = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { h = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(h ? Theme.ACCENT_SOFT : new Color(0, 0, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_SMALL, Theme.R_SMALL);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        addBtn.addActionListener(e -> {
            selectedPartId = p.getPartId();
            addPartToBuild();
        });
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        body.add(addBtn);

        ImageIcon icon = preloadedIcons.get(p.getPartId());
        if (icon == null) icon = ImageCache.getDefault(p.getCategoryId(), 80, 80);
        imgLbl.setIcon(icon);
        return card;
    }

    private void addPartToBuild() {
        if (selectedPartId == -1) {
            JOptionPane.showMessageDialog(this, "Select a part first.");
            return;
        }
        Part part = partDAO.getPartById(selectedPartId);
        if (part == null) return;
        if (part.getCategoryId() == 1) {
            selectedSocket = part.getSocketType();
            selectedDdrGen = part.getDdrGeneration();
            loadParts();
        }
        currentBuildParts.put(part.getCategoryId(), part.getPartId());
        JLabel lbl = partSlotLabels.get(part.getCategoryId());
        if (lbl != null) {
            lbl.setText(part.getBrand() + " " + part.getName());
            lbl.setForeground(Theme.TEXT);
        }
        JLabel icon = slotIconLabels.get(part.getCategoryId());
        if (icon != null) {
            icon.setText("x");
            icon.setForeground(Theme.EMBER);
        }
        updateTotals();
    }

    private void updateTotals() {
        int totalP = 0, totalS = 0;
        for (Map.Entry<Integer, Integer> e : currentBuildParts.entrySet()) {
            Part p = partDAO.getPartById(e.getValue());
            if (p != null) { totalP += p.getPrice(); totalS += p.getPerformanceScore(); }
        }
        totalPriceLabel.setText("PKR " + String.format("%,d", totalP));
        totalScoreLabel.setText(String.valueOf(totalS));
    }

    private void clearBuild() {
        currentBuildParts.clear();
        selectedSocket = null;
        selectedDdrGen = null;
        for (JLabel l : partSlotLabels.values()) { l.setText("Not selected"); l.setForeground(Theme.TEXT_3); }
        for (JLabel l : slotIconLabels.values()) { l.setText("+"); l.setForeground(Theme.TEXT_3); }
        totalPriceLabel.setText("PKR 0");
        totalScoreLabel.setText("0");
        loadParts();
    }

    private void saveBuild() {
        if (currentBuildParts.size() < 5) {
            JOptionPane.showMessageDialog(this, "Select all 5 parts before saving.");
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Build name:", "My Custom Build");
        if (name == null || name.trim().isEmpty()) return;
        int buildId = buildDAO.createBuild(name.trim());
        if (buildId == -1) { JOptionPane.showMessageDialog(this, "Failed to create build."); return; }
        int totalPrice = 0, totalScore = 0;
        for (Map.Entry<Integer, Integer> e : currentBuildParts.entrySet()) {
            Part p = partDAO.getPartById(e.getValue());
            if (p != null) { buildDAO.addPartToBuild(buildId, e.getKey(), e.getValue(), p.getPrice()); totalPrice += p.getPrice(); totalScore += p.getPerformanceScore(); }
        }
        buildDAO.updateBuildTotals(buildId, totalPrice, totalScore);
        JOptionPane.showMessageDialog(this, "Build saved: " + name);
        clearBuild();
        loadBuilds();
        dashboard.refreshStats();
    }

    private void loadBuilds() {
        buildsStrip.removeAll();
        List<Build> builds = buildDAO.getAllBuilds();
        if (builds.isEmpty()) {
            buildsStrip.add(new JLabel("No builds yet."));
            buildsStrip.revalidate();
            return;
        }
        for (Build b : builds) {
            buildsStrip.add(makeBuildChip(b));
            buildsStrip.add(Box.createHorizontalStrut(8));
        }
        buildsStrip.revalidate();
        buildsStrip.repaint();
    }

    private JPanel makeBuildChip(Build b) {
        JPanel chip = new JPanel(new BorderLayout(0, 4)) {
            private boolean hover = false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { selectedBuildId = b.getBuildId(); loadSelectedBuild(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? Theme.SURFACE_3 : Theme.SURFACE_2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_MEDIUM, Theme.R_MEDIUM);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        chip.setPreferredSize(new Dimension(170, 40));

        JLabel name = new JLabel(b.getName());
        name.setFont(Theme.regular(11));
        name.setForeground(Theme.TEXT);
        chip.add(name, BorderLayout.NORTH);

        JLabel detail = new JLabel(String.format("PKR %,d  ·  %d pts", b.getTotalPrice(), b.getTotalScore()));
        detail.setFont(Theme.mono(9));
        detail.setForeground(Theme.TEXT_3);
        chip.add(detail, BorderLayout.SOUTH);
        return chip;
    }

    private void loadSelectedBuild() {
        if (selectedBuildId == -1) return;
        clearBuild();
        List<BuildPart> parts = buildDAO.getBuildParts(selectedBuildId);
        for (BuildPart bp : parts) {
            currentBuildParts.put(bp.getCategoryId(), bp.getPartId());
            JLabel l = partSlotLabels.get(bp.getCategoryId());
            if (l != null) { l.setText(bp.getPartBrand() + " " + bp.getPartName()); l.setForeground(Theme.TEXT); }
            JLabel icon = slotIconLabels.get(bp.getCategoryId());
            if (icon != null) { icon.setText("x"); icon.setForeground(Theme.EMBER); }
            if (bp.getCategoryId() == 1) {
                Part cpu = partDAO.getPartById(bp.getPartId());
                if (cpu != null) { selectedSocket = cpu.getSocketType(); selectedDdrGen = cpu.getDdrGeneration(); }
            }
        }
        updateTotals();
        loadParts();
    }

    private void deleteSelectedBuild() {
        if (selectedBuildId == -1) { JOptionPane.showMessageDialog(this, "Click a saved build chip first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this build?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { buildDAO.deleteBuild(selectedBuildId); selectedBuildId = -1; loadBuilds(); dashboard.refreshStats(); }
    }

    private void loadImagesAsync() {
        if (currentImageLoader != null && !currentImageLoader.isDone()) currentImageLoader.cancel(true);
        preloadedIcons.clear();
        final List<Part> toLoad = new ArrayList<>();
        for (Part p : partById.values()) if (p.hasImage()) toLoad.add(p);
        if (toLoad.isEmpty()) return;
        currentImageLoader = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Part p : toLoad) {
                    if (isCancelled()) return null;
                    ImageIcon icon = ImageCache.get(p.getImagePath(), p.getCategoryId(), 80, 80);
                    if (isCancelled()) return null;
                    preloadedIcons.put(p.getPartId(), icon);
                    publish(p.getPartId());
                }
                return null;
            }
            @Override
            protected void process(List<Integer> chunks) {
                for (int partId : chunks) {
                    for (Component c : productGrid.getComponents()) {
                        if (c instanceof JPanel) {
                            for (Component inner : ((JPanel) c).getComponents()) {
                                if (inner instanceof JPanel) {
                                    for (Component deep : ((JPanel) inner).getComponents()) {
                                        if (deep instanceof JLabel && ((JLabel) deep).getIcon() != null) {
                                            Part p = partById.get(partId);
                                            if (p != null) ((JLabel) deep).setIcon(preloadedIcons.get(partId));
                                        }
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

    private void openImageEditor() {
        if (selectedPartId == -1) { JOptionPane.showMessageDialog(this, "Select a part first."); return; }
        Part part = partDAO.getPartById(selectedPartId);
        if (part == null) { JOptionPane.showMessageDialog(this, "Could not load part."); return; }
        ImageEditorDialog dlg = new ImageEditorDialog(SwingUtilities.getWindowAncestor(this), part);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            Part updated = partDAO.getPartById(selectedPartId);
            if (updated != null) {
                partById.put(updated.getPartId(), updated);
                if (searchField.getText().trim().isEmpty()) loadParts(); else filterParts();
            }
        }
    }
}
