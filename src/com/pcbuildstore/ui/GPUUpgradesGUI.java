package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.GPUOptionDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import com.pcbuildstore.models.GPUOption;
import com.pcbuildstore.models.Part;
import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class GPUUpgradesGUI extends JPanel {

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
        setBackground(Theme.BG);
        buildUI();
        loadBuilds();
    }

    public void onShow() { loadBuilds(); }

    private void buildUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
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
        JLabel ey = Components.eyebrow("PERFORMANCE");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("GPU upgrades");
        t.setFont(Theme.light(24));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(ey);
        left.add(Box.createVerticalStrut(2));
        left.add(t);
        row.add(left, BorderLayout.WEST);
        wrap.add(row);
        wrap.add(Components.vSpacer(12));
        wrap.add(createBuildSelectorRow());
        wrap.add(Components.vSpacer(12));
        return wrap;
    }

    private JPanel createBuildSelectorRow() {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Theme.SURFACE);
        card.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_LARGE));
        card.setOpaque(true);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        JLabel ey = Components.eyebrow("STEP  01");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("Pick a build to upgrade");
        t.setFont(Theme.bold(13));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(ey);
        left.add(Box.createVerticalStrut(2));
        left.add(t);
        card.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));

        JLabel sl = new JLabel("Build  ");
        sl.setFont(Theme.regular(10));
        sl.setForeground(Theme.TEXT_2);
        right.add(sl);

        JPanel cbWrap = new JPanel(new BorderLayout());
        cbWrap.setBackground(Theme.SURFACE_2);
        cbWrap.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM));
        cbWrap.setPreferredSize(new Dimension(300, 32));
        cbWrap.setMaximumSize(new Dimension(300, 32));
        buildSelector = new JComboBox<>();
        buildSelector.setFont(Theme.regular(11));
        buildSelector.setBackground(Theme.SURFACE_2);
        buildSelector.setForeground(Theme.TEXT);
        buildSelector.setBorder(null);
        buildSelector.setFocusable(false);
        buildSelector.addActionListener(e -> onBuildSelected());
        cbWrap.add(buildSelector, BorderLayout.CENTER);
        right.add(cbWrap);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.setBorder(BorderFactory.createEmptyBorder(0, 36, 18, 36));

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel title = new JLabel("Available upgrades");
        title.setFont(Theme.bold(13));
        title.setForeground(Theme.TEXT);
        headerRow.add(title, BorderLayout.WEST);
        left.add(headerRow, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.SURFACE);
        card.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_LARGE));

        String[] cols = {"ID", "GPU", "Brand", "Price +", "Score +", "New Total (PKR)"};
        optionsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        optionsTable = new JTable(optionsModel);
        styleTable(optionsTable);
        optionsTable.setRowHeight(36);

        optionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedOptionIndex = optionsTable.getSelectedRow();
            }
        });

        JScrollPane scroll = new JScrollPane(optionsTable);
        Components.applyDarkScrollbar(scroll);
        scroll.getViewport().setBackground(Theme.SURFACE);
        card.add(scroll, BorderLayout.CENTER);
        left.add(card, BorderLayout.CENTER);
        main.add(left, BorderLayout.CENTER);

        main.add(createSidePanel(), BorderLayout.EAST);
        return main;
    }

    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.BG);
        side.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        side.setPreferredSize(new Dimension(260, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ey = Components.eyebrow("STEP  02");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("Current GPU");
        t.setFont(Theme.bold(13));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(ey);
        header.add(Box.createVerticalStrut(2));
        header.add(t);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        side.add(header);
        side.add(Components.vSpacer(10));

        JPanel info = new JPanel(new BorderLayout(0, 8));
        info.setBackground(Theme.SURFACE);
        info.setBorder(BorderFactory.createCompoundBorder(
            new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        info.setOpaque(true);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(" - ");
        name.setFont(Theme.bold(13));
        name.setForeground(Theme.TEXT);
        info.add(name, BorderLayout.NORTH);
        currentGpuLabel = name;

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel pl = new JLabel("PRICE");
        pl.setFont(Theme.medium(8));
        pl.setForeground(Theme.TEXT_3);
        currentPriceLabel = new JLabel("PKR 0");
        currentPriceLabel.setFont(Theme.bold(15));
        currentPriceLabel.setForeground(Theme.TEXT);
        priceRow.add(pl, BorderLayout.NORTH);
        priceRow.add(currentPriceLabel, BorderLayout.CENTER);
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel scoreRow = new JPanel(new BorderLayout());
        scoreRow.setOpaque(false);
        scoreRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel sl = new JLabel("PERFORMANCE  SCORE");
        sl.setFont(Theme.medium(8));
        sl.setForeground(Theme.TEXT_3);
        currentScoreLabel = new JLabel("0");
        currentScoreLabel.setFont(Theme.bold(15));
        currentScoreLabel.setForeground(Theme.HOT);
        scoreRow.add(sl, BorderLayout.NORTH);
        scoreRow.add(currentScoreLabel, BorderLayout.CENTER);
        scoreRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        rows.add(priceRow);
        rows.add(Box.createVerticalStrut(4));
        rows.add(scoreRow);
        info.add(rows, BorderLayout.CENTER);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        side.add(info);
        side.add(Components.vSpacer(10));

        JButton apply = Components.primaryButton("Apply upgrade");
        apply.setAlignmentX(Component.LEFT_ALIGNMENT);
        apply.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        apply.setFont(Theme.medium(10));
        apply.addActionListener(e -> applyUpgrade());
        side.add(apply);
        side.add(Box.createVerticalGlue());
        return side;
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.SURFACE);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER_SOFT);
        table.setSelectionBackground(Theme.SURFACE_2);
        table.setSelectionForeground(Theme.TEXT);
        table.setFont(Theme.regular(12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);
        table.setFocusable(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.SURFACE);
        header.setForeground(Theme.TEXT_3);
        header.setFont(Theme.medium(9));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SOFT));
        header.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setBackground(sel ? Theme.SURFACE_2 : Theme.SURFACE);
                l.setForeground(Theme.TEXT);
                l.setFont(Theme.regular(12));
                l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (col == 3 || col == 5) {
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setFont(Theme.bold(12));
                } else if (col == 4) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setFont(Theme.bold(12));
                    l.setForeground(Theme.SUCCESS);
                } else if (col == 0) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setForeground(Theme.TEXT_3);
                }
                return l;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    private void loadBuilds() {
        buildSelector.removeAllItems();
        buildSelector.addItem("-- Select a build --");
        List<Build> builds = buildDAO.getAllBuilds();
        for (Build b : builds) {
            buildSelector.addItem(b.getBuildId() + "  -  " + b.getName() + "  -  PKR " + String.format("%,d", b.getTotalPrice()));
        }
    }

    private void onBuildSelected() {
        int idx = buildSelector.getSelectedIndex();
        if (idx <= 0) {
            selectedBuildId = -1;
            optionsModel.setRowCount(0);
            currentGpuLabel.setText(" - ");
            currentPriceLabel.setText("PKR 0");
            currentScoreLabel.setText("0");
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
                    currentPriceLabel.setText("PKR " + String.format("%,d", gpu.getPrice()));
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
                "+ " + String.format("%,d", g.getPriceIncrease()),
                "+ " + g.getPerformanceIncrease(),
                String.format("%,d", budget + g.getPriceIncrease())
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
            if (g.getGpuOptionId() == optionId) { selected = g; break; }
        }
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Apply upgrade: " + selected.getGpuName() + "?\n" +
            "Price increase: PKR " + String.format("%,d", selected.getPriceIncrease()) + "\n" +
            "Score increase: +" + selected.getPerformanceIncrease(),
            "Confirm upgrade", JOptionPane.YES_NO_OPTION);

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
}
