package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.CategoryDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import com.pcbuildstore.models.Category;
import com.pcbuildstore.models.Part;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class BuildCatalogGUI extends JPanel {

    private static final Color BG = DashboardGUI.BG;
    private static final Color CARD = DashboardGUI.CARD;
    private static final Color BORDER = DashboardGUI.BORDER;
    private static final Color TEXT = DashboardGUI.TEXT;
    private static final Color MUTED = DashboardGUI.MUTED;
    private static final Color BLUE = DashboardGUI.BLUE;
    private static final Color MINT = DashboardGUI.MINT;
    private static final Color EMBER = DashboardGUI.EMBER;
    private static final Color VIOLET = DashboardGUI.VIOLET;
    private static final Color INTEL = new Color(0, 113, 197);
    private static final Color AMD_RED = new Color(237, 28, 36);
    private static final Color NVIDIA = new Color(118, 185, 0);
    private static final Color AMD_ORANGE = new Color(240, 120, 0);

    private final DashboardGUI dashboard;
    private final PartDAO partDAO = new PartDAO();
    private final BuildDAO buildDAO = new BuildDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private DefaultTableModel partsModel;
    private JTable partsTable;
    private DefaultTableModel buildsModel;
    private JTable buildsTable;
    private JTextField searchField;
    private JLabel countLabel;

    private List<Category> categories;
    private int selectedCategoryId = 1;
    private int selectedPartId = -1;
    private int selectedBuildId = -1;

    private String selectedSocket = null;
    private String selectedDdrGen = null;

    private JPanel buildSummaryPanel;
    private JLabel totalPriceLabel;
    private JLabel totalScoreLabel;
    private Map<Integer, JLabel> partSlotLabels = new HashMap<>();
    private Map<Integer, Integer> currentBuildParts = new HashMap<>();

    public BuildCatalogGUI(DashboardGUI dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(BG);
        categories = categoryDAO.getAllCategories();
        buildUI();
        loadParts();
        loadBuilds();
    }

    private void buildUI() {
        add(createHeader(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel title = DashboardGUI.label("Build Configurator", TEXT, 28, Font.BOLD);
        header.add(title);
        header.add(Box.createHorizontalGlue());

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.setMaximumSize(new Dimension(200, 32));
        searchField.setFont(DashboardGUI.font(Font.PLAIN, 12));
        searchField.setBackground(CARD);
        searchField.setForeground(TEXT);
        searchField.setCaretColor(TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterParts(); }
            public void removeUpdate(DocumentEvent e) { filterParts(); }
            public void changedUpdate(DocumentEvent e) { filterParts(); }
        });
        header.add(searchField);
        header.add(Box.createRigidArea(new Dimension(15, 0)));

        countLabel = DashboardGUI.label("0 parts", MUTED, 12, Font.PLAIN);
        header.add(countLabel);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG);

        JPanel catTabs = createCategoryTabs();
        topPanel.add(catTabs, BorderLayout.NORTH);
        topPanel.add(createPartsTablePanel(), BorderLayout.CENTER);

        center.add(topPanel, BorderLayout.CENTER);
        center.add(createBuildsPanel(), BorderLayout.SOUTH);

        return center;
    }

    private JPanel createCategoryTabs() {
        JPanel tabs = new JPanel();
        tabs.setLayout(new BoxLayout(tabs, BoxLayout.X_AXIS));
        tabs.setBackground(BG);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        for (Category cat : categories) {
            JButton btn = new JButton(cat.getName());
            btn.setFont(DashboardGUI.font(Font.PLAIN, 12));
            btn.setBackground(CARD);
            btn.setForeground(MUTED);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(100, 32));

            final int catId = cat.getCategoryId();
            btn.addActionListener(e -> {
                selectedCategoryId = catId;
                selectedPartId = -1;
                loadParts();
                highlightTab(tabs, btn);
            });

            if (cat.getCategoryId() == selectedCategoryId) {
                btn.setBackground(BLUE);
                btn.setForeground(TEXT);
            }

            tabs.add(btn);
            tabs.add(Box.createRigidArea(new Dimension(5, 0)));
        }

        return tabs;
    }

    private void highlightTab(JPanel tabs, JButton active) {
        for (Component c : tabs.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b == active) {
                    b.setBackground(BLUE);
                    b.setForeground(TEXT);
                } else {
                    b.setBackground(CARD);
                    b.setForeground(MUTED);
                }
            }
        }
    }

    private JScrollPane createPartsTablePanel() {
        String[] cols = {"ID", "Brand", "Name", "Price (PKR)", "Score", "Details"};
        partsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        partsTable = new JTable(partsModel);
        styleTable(partsTable);

        partsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = partsTable.getSelectedRow();
                if (row >= 0) {
                    selectedPartId = (int) partsModel.getValueAt(row, 0);
                }
            }
        });

        partsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addPartToBuild();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(partsTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD);
        return scroll;
    }

    private JPanel createBuildsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.setPreferredSize(new Dimension(0, 200));

        JLabel label = DashboardGUI.label("Saved Builds", TEXT, 16, Font.BOLD);
        panel.add(label, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Price (PKR)", "Score", "Parts", "Created"};
        buildsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        buildsTable = new JTable(buildsModel);
        styleTable(buildsTable);

        buildsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = buildsTable.getSelectedRow();
                if (row >= 0) {
                    selectedBuildId = (int) buildsModel.getValueAt(row, 0);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(buildsTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel();
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setBackground(BG);
        btnRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton loadBtn = accentButton("Load Build", BLUE);
        loadBtn.addActionListener(e -> loadSelectedBuild());
        JButton deleteBtn = accentButton("Delete Build", EMBER);
        deleteBtn.addActionListener(e -> deleteSelectedBuild());

        btnRow.add(loadBtn);
        btnRow.add(Box.createRigidArea(new Dimension(10, 0)));
        btnRow.add(deleteBtn);
        btnRow.add(Box.createHorizontalGlue());

        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG);
        right.setPreferredSize(new Dimension(280, 0));
        right.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JLabel title = DashboardGUI.label("Current Build", TEXT, 16, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(title);
        right.add(Box.createRigidArea(new Dimension(0, 15)));

        String[] slotNames = {"CPU", "GPU", "RAM", "Storage", "PSU"};
        int[] slotIds = {1, 2, 3, 4, 5};
        Color[] slotColors = {BLUE, NVIDIA, MINT, VIOLET, EMBER};

        for (int i = 0; i < slotNames.length; i++) {
            JPanel slot = createPartSlot(slotNames[i], slotIds[i], slotColors[i]);
            slot.setAlignmentX(Component.LEFT_ALIGNMENT);
            right.add(slot);
            right.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        right.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(CARD);
        totals.setAlignmentX(Component.LEFT_ALIGNMENT);
        totals.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        totals.add(DashboardGUI.label("Total Price:", MUTED, 12, Font.PLAIN), gc);
        gc.gridx = 1;
        totalPriceLabel = DashboardGUI.label("PKR 0", MINT, 14, Font.BOLD);
        totals.add(totalPriceLabel, gc);

        gc.gridx = 0; gc.gridy = 1;
        totals.add(DashboardGUI.label("Total Score:", MUTED, 12, Font.PLAIN), gc);
        gc.gridx = 1;
        totalScoreLabel = DashboardGUI.label("0", VIOLET, 14, Font.BOLD);
        totals.add(totalScoreLabel, gc);

        right.add(totals);
        right.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addBtn = accentButton("Add Selected Part", MINT);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> addPartToBuild());
        right.add(addBtn);
        right.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton clearBtn = accentButton("Clear Build", EMBER);
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.addActionListener(e -> clearBuild());
        right.add(clearBtn);
        right.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton saveBtn = accentButton("Save Build", BLUE);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveBuild());
        right.add(saveBtn);
        right.add(Box.createVerticalGlue());

        return right;
    }

    private JPanel createPartSlot(String name, int categoryId, Color accent) {
        JPanel slot = new JPanel(new BorderLayout());
        slot.setBackground(CARD);
        slot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        slot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel catLabel = DashboardGUI.label(name, accent, 11, Font.BOLD);
        slot.add(catLabel, BorderLayout.NORTH);

        JLabel partLabel = DashboardGUI.label("Not selected", MUTED, 11, Font.PLAIN);
        slot.add(partLabel, BorderLayout.CENTER);

        partSlotLabels.put(categoryId, partLabel);

        return slot;
    }

    private void loadParts() {
        partsModel.setRowCount(0);
        List<Part> parts;

        if (selectedCategoryId == 3 && selectedDdrGen != null) {
            parts = partDAO.getCompatibleParts(selectedSocket, selectedDdrGen);
        } else {
            parts = partDAO.getPartsByCategory(selectedCategoryId);
        }

        for (Part p : parts) {
            String details = getPartDetails(p);
            partsModel.addRow(new Object[]{
                p.getPartId(), p.getBrand(), p.getName(),
                p.getPrice(), p.getPerformanceScore(), details
            });
        }
        countLabel.setText(parts.size() + " parts");
    }

    private String getPartDetails(Part p) {
        switch (p.getCategoryId()) {
            case 1: return p.getSocketType() + " | " + p.getDdrGeneration() + " | " + p.getCoreCount() + "C | " + p.getClockSpeed();
            case 2: return p.getVram() + " VRAM";
            case 3: return p.getDdrGeneration() + " | " + p.getMemorySpeed() + " | " + p.getCapacity();
            case 4: return p.getCapacity() + " | " + p.getReadSpeed();
            case 5: return p.getWattage() + "W | " + p.getEfficiency();
            default: return "";
        }
    }

    private void filterParts() {
        String query = searchField.getText().trim().toLowerCase();
        partsModel.setRowCount(0);

        List<Part> parts;
        if (selectedCategoryId == 3 && selectedDdrGen != null) {
            parts = partDAO.getCompatibleParts(selectedSocket, selectedDdrGen);
        } else {
            parts = partDAO.getPartsByCategory(selectedCategoryId);
        }

        for (Part p : parts) {
            if (query.isEmpty() || p.getName().toLowerCase().contains(query)
                    || p.getBrand().toLowerCase().contains(query)) {
                partsModel.addRow(new Object[]{
                    p.getPartId(), p.getBrand(), p.getName(),
                    p.getPrice(), p.getPerformanceScore(), getPartDetails(p)
                });
            }
        }
        countLabel.setText(partsModel.getRowCount() + " parts");
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
        partSlotLabels.get(part.getCategoryId()).setText(part.getBrand() + " " + part.getName());
        partSlotLabels.get(part.getCategoryId()).setForeground(TEXT);

        updateTotals();
    }

    private void updateTotals() {
        int totalP = 0;
        int totalS = 0;
        for (Map.Entry<Integer, Integer> e : currentBuildParts.entrySet()) {
            Part p = partDAO.getPartById(e.getValue());
            if (p != null) {
                totalP += p.getPrice();
                totalS += p.getPerformanceScore();
            }
        }
        totalPriceLabel.setText(String.format("PKR %,d", totalP));
        totalScoreLabel.setText(String.valueOf(totalS));
    }

    private void clearBuild() {
        currentBuildParts.clear();
        selectedSocket = null;
        selectedDdrGen = null;
        for (JLabel l : partSlotLabels.values()) {
            l.setText("Not selected");
            l.setForeground(MUTED);
        }
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
        if (buildId == -1) {
            JOptionPane.showMessageDialog(this, "Failed to create build.");
            return;
        }

        int totalPrice = 0;
        int totalScore = 0;
        for (Map.Entry<Integer, Integer> e : currentBuildParts.entrySet()) {
            Part p = partDAO.getPartById(e.getValue());
            if (p != null) {
                buildDAO.addPartToBuild(buildId, e.getKey(), e.getValue(), p.getPrice());
                totalPrice += p.getPrice();
                totalScore += p.getPerformanceScore();
            }
        }

        buildDAO.updateBuildTotals(buildId, totalPrice, totalScore);
        JOptionPane.showMessageDialog(this, "Build saved: " + name);

        clearBuild();
        loadBuilds();
        dashboard.refreshStats();
    }

    private void loadBuilds() {
        buildsModel.setRowCount(0);
        List<Build> builds = buildDAO.getAllBuilds();
        for (Build b : builds) {
            int partCount = buildDAO.getPartCountInBuild(b.getBuildId());
            buildsModel.addRow(new Object[]{
                b.getBuildId(), b.getName(), b.getTotalPrice(),
                b.getTotalScore(), partCount,
                b.getCreatedAt() != null ? b.getCreatedAt().toString().substring(0, 16) : ""
            });
        }
    }

    private void loadSelectedBuild() {
        if (selectedBuildId == -1) {
            JOptionPane.showMessageDialog(this, "Select a build first.");
            return;
        }

        clearBuild();

        List<BuildPart> parts = buildDAO.getBuildParts(selectedBuildId);
        for (BuildPart bp : parts) {
            currentBuildParts.put(bp.getCategoryId(), bp.getPartId());
            JLabel label = partSlotLabels.get(bp.getCategoryId());
            if (label != null) {
                label.setText(bp.getPartBrand() + " " + bp.getPartName());
                label.setForeground(TEXT);
            }
            if (bp.getCategoryId() == 1) {
                Part cpu = partDAO.getPartById(bp.getPartId());
                if (cpu != null) {
                    selectedSocket = cpu.getSocketType();
                    selectedDdrGen = cpu.getDdrGeneration();
                }
            }
        }
        updateTotals();
        loadParts();
    }

    private void deleteSelectedBuild() {
        if (selectedBuildId == -1) {
            JOptionPane.showMessageDialog(this, "Select a build first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this build?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            buildDAO.deleteBuild(selectedBuildId);
            selectedBuildId = -1;
            loadBuilds();
            dashboard.refreshStats();
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(BLUE.darker());
        table.setSelectionForeground(TEXT);
        table.setFont(DashboardGUI.font(Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 15, 22));
        header.setForeground(MUTED);
        header.setFont(DashboardGUI.font(Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setBackground(CARD);
        center.setForeground(TEXT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    private JButton accentButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(DashboardGUI.font(Font.BOLD, 12));
        btn.setBackground(accent);
        btn.setForeground(new Color(8, 8, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 35));
        return btn;
    }
}
