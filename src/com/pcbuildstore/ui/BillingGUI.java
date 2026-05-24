package com.pcbuildstore.ui;

import com.pcbuildstore.dao.BillDAO;
import com.pcbuildstore.dao.BuildDAO;
import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Bill;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
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
import java.time.format.DateTimeFormatter;
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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class BillingGUI extends JPanel {

    private static final Color BG = DashboardGUI.BG;
    private static final Color CARD = DashboardGUI.CARD;
    private static final Color BORDER = DashboardGUI.BORDER;
    private static final Color TEXT = DashboardGUI.TEXT;
    private static final Color MUTED = DashboardGUI.MUTED;
    private static final Color EMBER = DashboardGUI.EMBER;
    private static final Color MINT = DashboardGUI.MINT;
    private static final Color BLUE = DashboardGUI.BLUE;
    private static final Color VIOLET = DashboardGUI.VIOLET;

    private final DashboardGUI dashboard;
    private final BillDAO billDAO = new BillDAO();
    private final BuildDAO buildDAO = new BuildDAO();
    private final PartDAO partDAO = new PartDAO();

    private JComboBox<String> buildSelector;
    private DefaultTableModel billsModel;
    private JTable billsTable;
    private int selectedBuildId = -1;
    private int selectedBillId = -1;

    private JLabel previewName;
    private JLabel previewCpu;
    private JLabel previewGpu;
    private JLabel previewRam;
    private JLabel previewStorage;
    private JLabel previewPsu;
    private JLabel previewPrice;
    private JLabel previewScore;

    public BillingGUI(DashboardGUI dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        loadBills();
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

        JLabel title = DashboardGUI.label("Billing", TEXT, 28, Font.BOLD);
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

        JLabel selectLabel = DashboardGUI.label("Select Build to Purchase:", TEXT, 13, Font.PLAIN);
        top.add(selectLabel);
        top.add(Box.createRigidArea(new Dimension(10, 0)));

        buildSelector = new JComboBox<>();
        buildSelector.setFont(DashboardGUI.font(Font.PLAIN, 12));
        buildSelector.setBackground(CARD);
        buildSelector.setForeground(TEXT);
        buildSelector.setPreferredSize(new Dimension(350, 32));
        buildSelector.addActionListener(e -> onBuildSelected());
        top.add(buildSelector);

        top.add(Box.createHorizontalGlue());

        return top;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);

        JLabel label = DashboardGUI.label("Purchase History", TEXT, 14, Font.BOLD);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        center.add(label, BorderLayout.NORTH);

        String[] cols = {"Bill ID", "Build", "Price (PKR)", "Score", "Date"};
        billsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        billsTable = new JTable(billsModel);
        styleTable(billsTable);

        billsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = billsTable.getSelectedRow();
                selectedBillId = row >= 0 ? (int) billsModel.getValueAt(row, 0) : -1;
            }
        });

        JScrollPane scroll = new JScrollPane(billsTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD);
        center.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel();
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setBackground(BG);
        btnRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton receiptBtn = accentButton("View Receipt", VIOLET);
        receiptBtn.addActionListener(e -> viewReceipt());
        JButton deleteBtn = accentButton("Delete Bill", EMBER);
        deleteBtn.addActionListener(e -> deleteBill());

        btnRow.add(receiptBtn);
        btnRow.add(Box.createRigidArea(new Dimension(10, 0)));
        btnRow.add(deleteBtn);
        btnRow.add(Box.createHorizontalGlue());

        center.add(btnRow, BorderLayout.SOUTH);

        return center;
    }

    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG);
        side.setPreferredSize(new Dimension(280, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(0, 15, 0, 0)
        ));

        JLabel title = DashboardGUI.label("Build Preview", TEXT, 14, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);
        side.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel previewCard = new JPanel(new GridBagLayout());
        previewCard.setBackground(CARD);
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        previewCard.add(DashboardGUI.label("Name:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1; gc.weightx = 1;
        previewName = DashboardGUI.label("--", TEXT, 11, Font.BOLD);
        previewCard.add(previewName, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        previewCard.add(DashboardGUI.label("CPU:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewCpu = DashboardGUI.label("--", TEXT, 11, Font.PLAIN);
        previewCard.add(previewCpu, gc);

        gc.gridx = 0; gc.gridy = 2;
        previewCard.add(DashboardGUI.label("GPU:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewGpu = DashboardGUI.label("--", TEXT, 11, Font.PLAIN);
        previewCard.add(previewGpu, gc);

        gc.gridx = 0; gc.gridy = 3;
        previewCard.add(DashboardGUI.label("RAM:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewRam = DashboardGUI.label("--", TEXT, 11, Font.PLAIN);
        previewCard.add(previewRam, gc);

        gc.gridx = 0; gc.gridy = 4;
        previewCard.add(DashboardGUI.label("Storage:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewStorage = DashboardGUI.label("--", TEXT, 11, Font.PLAIN);
        previewCard.add(previewStorage, gc);

        gc.gridx = 0; gc.gridy = 5;
        previewCard.add(DashboardGUI.label("PSU:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewPsu = DashboardGUI.label("--", TEXT, 11, Font.PLAIN);
        previewCard.add(previewPsu, gc);

        gc.gridx = 0; gc.gridy = 6;
        previewCard.add(DashboardGUI.label("Price:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewPrice = DashboardGUI.label("--", MINT, 12, Font.BOLD);
        previewCard.add(previewPrice, gc);

        gc.gridx = 0; gc.gridy = 7;
        previewCard.add(DashboardGUI.label("Score:", MUTED, 10, Font.PLAIN), gc);
        gc.gridx = 1;
        previewScore = DashboardGUI.label("--", VIOLET, 12, Font.BOLD);
        previewCard.add(previewScore, gc);

        side.add(previewCard);
        side.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton purchaseBtn = accentButton("Purchase Build", MINT);
        purchaseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        purchaseBtn.addActionListener(e -> purchaseBuild());
        side.add(purchaseBtn);

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
            clearPreview();
            return;
        }

        Build build = buildDAO.getAllBuilds().get(idx - 1);
        selectedBuildId = build.getBuildId();

        previewName.setText(build.getName());
        previewPrice.setText(String.format("PKR %,d", build.getTotalPrice()));
        previewScore.setText(String.valueOf(build.getTotalScore()));

        List<BuildPart> parts = buildDAO.getBuildParts(selectedBuildId);
        for (BuildPart bp : parts) {
            Part p = partDAO.getPartById(bp.getPartId());
            if (p == null) continue;
            String text = p.getBrand() + " " + p.getName();
            switch (bp.getCategoryId()) {
                case 1: previewCpu.setText(text); break;
                case 2: previewGpu.setText(text); break;
                case 3: previewRam.setText(text); break;
                case 4: previewStorage.setText(text); break;
                case 5: previewPsu.setText(text); break;
            }
        }
    }

    private void clearPreview() {
        previewName.setText("--");
        previewCpu.setText("--");
        previewGpu.setText("--");
        previewRam.setText("--");
        previewStorage.setText("--");
        previewPsu.setText("--");
        previewPrice.setText("--");
        previewScore.setText("--");
    }

    private void purchaseBuild() {
        if (selectedBuildId == -1) {
            JOptionPane.showMessageDialog(this, "Select a build to purchase.");
            return;
        }

        Build build = buildDAO.getBuildById(selectedBuildId);
        List<BuildPart> parts = buildDAO.getBuildParts(selectedBuildId);
        if (parts.size() < 5) {
            JOptionPane.showMessageDialog(this, "This build is incomplete (missing parts).");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Purchase this build?\n\n");
        sb.append("Name: ").append(build.getName()).append("\n");
        sb.append("Price: PKR ").append(String.format("%,d", build.getTotalPrice())).append("\n");
        sb.append("Score: ").append(build.getTotalScore()).append("\n\n");
        sb.append("Parts:\n");
        for (BuildPart bp : parts) {
            Part p = partDAO.getPartById(bp.getPartId());
            if (p != null) {
                sb.append("  ").append(bp.getCategoryName()).append(": ")
                  .append(p.getBrand()).append(" ").append(p.getName())
                  .append(" (PKR ").append(String.format("%,d", bp.getPriceAtAdd())).append(")\n");
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(), "Confirm Purchase", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Bill bill = new Bill(0, selectedBuildId, build.getTotalPrice(), build.getTotalScore(), null);
            boolean saved = billDAO.saveBill(bill);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Purchase complete!");
                loadBills();
                loadBuilds();
                clearPreview();
                selectedBuildId = -1;
                buildSelector.setSelectedIndex(0);
                dashboard.refreshStats();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save bill.");
            }
        }
    }

    private void loadBills() {
        billsModel.setRowCount(0);
        List<Bill> bills = billDAO.getAllBills();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Bill b : bills) {
            billsModel.addRow(new Object[]{
                b.getBillId(),
                b.getBuildName(),
                b.getFinalPrice(),
                b.getFinalScore(),
                b.getPurchaseDate() != null ? b.getPurchaseDate().format(fmt) : ""
            });
        }
    }

    private void viewReceipt() {
        if (selectedBillId == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }

        Bill bill = null;
        for (Bill b : billDAO.getAllBills()) {
            if (b.getBillId() == selectedBillId) {
                bill = b;
                break;
            }
        }
        if (bill == null) return;

        Build build = buildDAO.getBuildById(bill.getBuildId());
        List<BuildPart> parts = buildDAO.getBuildParts(bill.getBuildId());

        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("          PC BUILD STORE RECEIPT         \n");
        receipt.append("========================================\n\n");
        receipt.append("Bill #").append(bill.getBillId()).append("\n");
        receipt.append("Date: ").append(bill.getPurchaseDate() != null ?
            bill.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").append("\n\n");
        receipt.append("Build: ").append(bill.getBuildName()).append("\n");
        receipt.append("----------------------------------------\n");

        for (BuildPart bp : parts) {
            Part p = partDAO.getPartById(bp.getPartId());
            if (p != null) {
                receipt.append(String.format("%-10s %s %s\n", bp.getCategoryName() + ":", p.getBrand(), p.getName()));
                receipt.append(String.format("%-10s PKR %,d\n", "", bp.getPriceAtAdd()));
            }
        }

        receipt.append("----------------------------------------\n");
        receipt.append(String.format("%-10s PKR %,d\n", "TOTAL:", bill.getFinalPrice()));
        receipt.append(String.format("%-10s %d\n", "SCORE:", bill.getFinalScore()));
        receipt.append("\n========================================\n");
        receipt.append("       Thank you for your purchase!     \n");
        receipt.append("========================================\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(DashboardGUI.font(Font.PLAIN, 13));
        textArea.setBackground(new Color(15, 15, 20));
        textArea.setForeground(MINT);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, textArea, "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteBill() {
        if (selectedBillId == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this bill?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            billDAO.deleteBill(selectedBillId);
            selectedBillId = -1;
            loadBills();
            dashboard.refreshStats();
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(EMBER.darker());
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
        btn.setPreferredSize(new Dimension(250, 38));
        return btn;
    }
}
