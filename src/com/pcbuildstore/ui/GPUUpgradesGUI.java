package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.GPUOptionDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import com.pcbuildstore.models.GPUOption;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class GPUUpgradesGUI extends JPanel {

    private static final Color BG = DashboardGUI.BG;
    private static final Color CARD = DashboardGUI.CARD;
    private static final Color BORDER = DashboardGUI.BORDER;
    private static final Color TEXT = DashboardGUI.TEXT;
    private static final Color MUTED = DashboardGUI.MUTED;
    private static final Color VIOLET = DashboardGUI.VIOLET;
    private static final Color MINT = DashboardGUI.MINT;
    private static final Color EMBER = DashboardGUI.EMBER;
    private static final Color NVIDIA = new Color(118, 185, 0);
    private static final Color AMD_ORANGE = new Color(240, 120, 0);

    private final DashboardGUI dashboard;
    private final GPUOptionDAO gpuOptionDAO = new GPUOptionDAO();
    private final BuildDAO buildDAO = new BuildDAO();
    private final PartDAO partDAO = new PartDAO();

    private JComboBox<String> buildSelector;
    private DefaultTableModel optionsModel;
    private JTable optionsTable;
    private JLabel currentGpuLabel;
    private JLabel currentPriceLabel;
    private JLabel currentScoreLabel;
    private int selectedBuildId = -1;
    private int selectedOptionIndex = -1;

    public GPUUpgradesGUI(DashboardGUI dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        loadBuilds();
    }

    private void buildUI() {
        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel title = DashboardGUI.label("GPU Upgrades", TEXT, 28, Font.BOLD);
        header.add(title);
        header.add(Box.createHorizontalGlue());

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        main.add(createTopPanel(), BorderLayout.NORTH);
        main.add(createCenterPanel(), BorderLayout.CENTER);
        main.add(createSidePanel(), BorderLayout.EAST);

        return main;
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBackground(BG);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel selectLabel = DashboardGUI.label("Select Build:", TEXT, 13, Font.PLAIN);
        top.add(selectLabel);
        top.add(Box.createRigidArea(new Dimension(10, 0)));

        buildSelector = new JComboBox<>();
        buildSelector.setFont(DashboardGUI.font(Font.PLAIN, 12));
        buildSelector.setBackground(CARD);
        buildSelector.setForeground(TEXT);
        buildSelector.setPreferredSize(new Dimension(300, 32));
        buildSelector.addActionListener(e -> onBuildSelected());
        top.add(buildSelector);

        top.add(Box.createHorizontalGlue());

        return top;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);

        JLabel label = DashboardGUI.label("Available GPU Upgrades", TEXT, 14, Font.BOLD);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        center.add(label, BorderLayout.NORTH);

        String[] cols = {"ID", "GPU", "Brand", "Price Increase", "Score Increase", "New Total"};
        optionsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        optionsTable = new JTable(optionsModel);
        styleTable(optionsTable);

        optionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedOptionIndex = optionsTable.getSelectedRow();
            }
        });

        JScrollPane scroll = new JScrollPane(optionsTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD);
        center.add(scroll, BorderLayout.CENTER);

        return center;
    }

    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG);
        side.setPreferredSize(new Dimension(260, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(0, 15, 0, 0)
        ));

        JLabel title = DashboardGUI.label("Current GPU", TEXT, 14, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);
        side.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel infoCard = new JPanel(new GridBagLayout());
        infoCard.setBackground(CARD);
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1;
        infoCard.add(DashboardGUI.label("GPU:", MUTED, 11, Font.PLAIN), gc);
        gc.gridx = 1;
        currentGpuLabel = DashboardGUI.label("--", TEXT, 12, Font.BOLD);
        infoCard.add(currentGpuLabel, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        infoCard.add(DashboardGUI.label("Price:", MUTED, 11, Font.PLAIN), gc);
        gc.gridx = 1; gc.weightx = 1;
        currentPriceLabel = DashboardGUI.label("--", MINT, 12, Font.BOLD);
        infoCard.add(currentPriceLabel, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        infoCard.add(DashboardGUI.label("Score:", MUTED, 11, Font.PLAIN), gc);
        gc.gridx = 1;
        currentScoreLabel = DashboardGUI.label("--", VIOLET, 12, Font.BOLD);
        infoCard.add(currentScoreLabel, gc);

        side.add(infoCard);
        side.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton applyBtn = accentButton("Apply Upgrade", VIOLET);
        applyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyBtn.addActionListener(e -> applyUpgrade());
        side.add(applyBtn);

        side.add(Box.createVerticalGlue());

        return side;
    }

    private void loadBuilds() {
        buildSelector.removeAllItems();
        buildSelector.addItem("-- Select a Build --");
        List<Build> builds = buildDAO.getAllBuilds();
        for (Build b : builds) {
            buildSelector.addItem(b.getBuildId() + " | " + b.getName() + " | PKR " + b.getTotalPrice());
        }
    }

    private void onBuildSelected() {
        int idx = buildSelector.getSelectedIndex();
        if (idx <= 0) {
            selectedBuildId = -1;
            optionsModel.setRowCount(0);
            currentGpuLabel.setText("--");
            currentPriceLabel.setText("--");
            currentScoreLabel.setText("--");
            return;
        }

        Build build = buildDAO.getAllBuilds().get(idx - 1);
        selectedBuildId = build.getBuildId();

        List<BuildPart> parts = buildDAO.getBuildParts(selectedBuildId);
        for (BuildPart bp : parts) {
            if (bp.getCategoryId() == 2) {
                Part gpu = partDAO.getPartById(bp.getPartId());
                if (gpu != null) {
                    currentGpuLabel.setText(gpu.getBrand() + " " + gpu.getName());
                    currentPriceLabel.setText(String.format("PKR %,d", gpu.getPrice()));
                    currentScoreLabel.setText(String.valueOf(gpu.getPerformanceScore()));
                }
                break;
            }
        }

        loadGPUOptions(build.getTotalPrice());
    }

    private void loadGPUOptions(int budget) {
        optionsModel.setRowCount(0);
        List<GPUOption> options = gpuOptionDAO.getGPUOptionsByBudget(budget);
        for (GPUOption g : options) {
            optionsModel.addRow(new Object[]{
                g.getGpuOptionId(),
                g.getGpuName(),
                g.getGpuBrand(),
                g.getPriceIncrease(),
                g.getPerformanceIncrease(),
                budget + g.getPriceIncrease()
            });
        }
    }

    private void applyUpgrade() {
        if (selectedBuildId == -1) {
            JOptionPane.showMessageDialog(this, "Select a build first.");
            return;
        }
        if (selectedOptionIndex < 0 || selectedOptionIndex >= optionsModel.getRowCount()) {
            JOptionPane.showMessageDialog(this, "Select a GPU upgrade from the table.");
            return;
        }

        int optionId = (int) optionsModel.getValueAt(selectedOptionIndex, 0);
        GPUOption selected = null;
        for (GPUOption g : gpuOptionDAO.getGPUOptionsByBudget(
                buildDAO.getBuildById(selectedBuildId).getTotalPrice())) {
            if (g.getGpuOptionId() == optionId) {
                selected = g;
                break;
            }
        }
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Apply upgrade: " + selected.getGpuName() + "?\n" +
            "Price increase: PKR " + selected.getPriceIncrease() + "\n" +
            "Score increase: +" + selected.getPerformanceIncrease(),
            "Confirm Upgrade", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            buildDAO.removePartFromBuild(selectedBuildId, 2);
            buildDAO.addPartToBuild(selectedBuildId, 2, selected.getGpuPartId(),
                partDAO.getPartById(selected.getGpuPartId()).getPrice());

            Build build = buildDAO.getBuildById(selectedBuildId);
            int newPrice = build.getTotalPrice() + selected.getPriceIncrease();
            int newScore = build.getTotalScore() + selected.getPerformanceIncrease();
            buildDAO.updateBuildTotals(selectedBuildId, newPrice, newScore);

            JOptionPane.showMessageDialog(this, "GPU upgraded successfully!");
            onBuildSelected();
            dashboard.refreshStats();
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(VIOLET.darker());
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
        btn.setPreferredSize(new Dimension(230, 38));
        return btn;
    }
}
