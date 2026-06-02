package com.pcbuildstore.ui;

import com.pcbuildstore.dao.PartDAO;
import com.pcbuildstore.models.Part;
import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;
import com.pcbuildstore.util.ImageCache;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ImageEditorDialog extends JDialog {

    private final Part part;
    private final PartDAO partDAO = new PartDAO();
    private JTextField urlField;
    private JLabel previewLabel;
    private JLabel statusLabel;
    private boolean saved = false;

    public ImageEditorDialog(Window owner, Part part) {
        super(owner, "Set image", ModalityType.APPLICATION_MODAL);
        this.part = part;

        setSize(520, 360);
        setMinimumSize(new Dimension(480, 320));
        setLocationRelativeTo(owner);
        setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createCenter(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });

        if (part.hasImage()) {
            setStatus("Loading current image…", Theme.TEXT_3);
            loadPreviewAsync(part.getImagePath());
        }
    }

    private JPanel createHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel ey = Components.eyebrow("MEDIA");
        ey.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(part.getBrand() + "  " + part.getName());
        title.setFont(Theme.bold(16));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Paste a direct image URL or leave empty for the category default.");
        sub.setFont(Theme.regular(10));
        sub.setForeground(Theme.TEXT_3);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(ey);
        p.add(Box.createVerticalStrut(2));
        p.add(title);
        p.add(Box.createVerticalStrut(2));
        p.add(sub);
        return p;
    }

    private JPanel createCenter() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 0, 12, 0));

        JPanel previewCard = new JPanel(new BorderLayout());
        previewCard.setBackground(Theme.SURFACE_2);
        previewCard.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM));
        previewCard.setPreferredSize(new Dimension(140, 140));
        previewCard.setMinimumSize(new Dimension(140, 140));
        previewCard.setMaximumSize(new Dimension(140, 140));
        previewCard.setOpaque(true);

        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setIcon(ImageCache.getDefault(part.getCategoryId(), 100, 100));
        previewCard.add(previewLabel, BorderLayout.CENTER);
        p.add(previewCard);
        p.add(Box.createHorizontalStrut(18));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel urlLabel = new JLabel("IMAGE  URL");
        urlLabel.setFont(Theme.medium(9));
        urlLabel.setForeground(Theme.TEXT_3);
        urlLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(urlLabel);
        right.add(Box.createVerticalStrut(6));

        JPanel tfWrap = new JPanel(new BorderLayout());
        tfWrap.setBackground(Theme.SURFACE_2);
        tfWrap.setBorder(new Components.RoundedBorder(Theme.BORDER, 1, Theme.R_MEDIUM));
        tfWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        urlField = new JTextField(part.hasImage() ? part.getImagePath() : "");
        urlField.setOpaque(false);
        urlField.setFont(Theme.regular(12));
        urlField.setForeground(Theme.TEXT);
        urlField.setCaretColor(Theme.TEXT);
        urlField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { schedulePreview(); }
            public void removeUpdate(DocumentEvent e) { schedulePreview(); }
            public void changedUpdate(DocumentEvent e) { schedulePreview(); }
        });
        tfWrap.add(urlField, BorderLayout.CENTER);
        right.add(tfWrap);
        right.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.regular(10));
        statusLabel.setForeground(Theme.TEXT_3);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(statusLabel);
        right.add(Box.createVerticalGlue());

        p.add(right);
        return p;
    }

    private JPanel createFooter() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton resetBtn = Components.ghostButton("Reset to default");
        resetBtn.setForeground(Theme.SALE);
        resetBtn.addActionListener(e -> {
            urlField.setText("");
            setStatus("Will reset to the category default on save.", Theme.TEXT_3);
        });

        JButton cancelBtn = Components.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = Components.primaryButton("Save");
        saveBtn.addActionListener(e -> save());

        p.add(resetBtn);
        p.add(Box.createHorizontalGlue());
        p.add(cancelBtn);
        p.add(Box.createHorizontalStrut(8));
        p.add(saveBtn);
        return p;
    }

    private void save() {
        String url = urlField.getText().trim();
        boolean ok = partDAO.updateImagePath(part.getPartId(), url.isEmpty() ? null : url);
        if (ok) {
            saved = true;
            dispose();
        } else {
            setStatus("Failed to save. Check the database connection.", Theme.SALE);
        }
    }

    private void schedulePreview() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            previewLabel.setIcon(ImageCache.getDefault(part.getCategoryId(), 100, 100));
            setStatus("Empty — category default will be used.", Theme.TEXT_3);
            return;
        }
        setStatus("Loading…", Theme.TEXT_3);
        loadPreviewAsync(url);
    }

    private void loadPreviewAsync(String url) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return ImageCache.get(url, part.getCategoryId(), 100, 100);
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    previewLabel.setIcon(icon);
                    boolean isDefault = icon == ImageCache.getDefault(part.getCategoryId(), 100, 100);
                    if (isDefault && !url.isEmpty()) {
                        setStatus("Could not load — URL saved anyway (default shown on failure).", Theme.SALE);
                    } else {
                        setStatus("Loaded successfully.", Theme.SUCCESS);
                    }
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), Theme.SALE);
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    public boolean isSaved() { return saved; }
}
