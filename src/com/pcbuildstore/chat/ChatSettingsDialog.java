package com.pcbuildstore.chat;

import com.pcbuildstore.ui.theme.Components;
import com.pcbuildstore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatSettingsDialog extends JDialog {

    private final ChatConfig config;
    private final JTextField baseUrl = new JTextField();
    private final JPasswordField apiKey = new JPasswordField();
    private final JTextField model = new JTextField();
    private final JTextArea systemPrompt = new JTextArea(5, 30);
    private boolean saved = false;

    public ChatSettingsDialog(Window owner, ChatConfig config) {
        super(owner, "PC Assistant  -  Settings", ModalityType.APPLICATION_MODAL);
        this.config = config;
        setSize(560, 460);
        setLocationRelativeTo(owner);
        setBackground(Theme.BG);
        buildUI();
    }

    public boolean isSaved() { return saved; }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Chat backend");
        title.setFont(Theme.bold(16));
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Connect any OpenAI-compatible /v1/chat/completions endpoint.");
        sub.setFont(Theme.regular(11));
        sub.setForeground(Theme.TEXT_3);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        head.add(title);
        head.add(sub);
        root.add(head, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        baseUrl.setText(config.baseUrl);
        model.setText(config.model);
        apiKey.setText(config.apiKey);
        systemPrompt.setText(config.systemPrompt);
        systemPrompt.setLineWrap(true);
        systemPrompt.setWrapStyleWord(true);

        form.add(field("Base URL", baseUrl, "e.g. https://api.openai.com/v1 or http://localhost:11434/v1"));
        form.add(Box.createVerticalStrut(10));
        form.add(field("API key", apiKey, "Bearer token (leave empty to disable)"));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Model", model, "e.g. gpt-4o-mini, llama3.1, mistral-nemo"));
        form.add(Box.createVerticalStrut(10));

        JLabel sysLbl = new JLabel("SYSTEM PROMPT");
        sysLbl.setFont(Theme.medium(8));
        sysLbl.setForeground(Theme.TEXT_3);
        sysLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sysLbl);
        form.add(Box.createVerticalStrut(4));

        JScrollPane sp = new JScrollPane(systemPrompt);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        Components.applyDarkScrollbar(sp);
        sp.getViewport().setBackground(Theme.SURFACE_2);
        systemPrompt.setBackground(Theme.SURFACE_2);
        systemPrompt.setForeground(Theme.TEXT);
        systemPrompt.setCaretColor(Theme.ACCENT);
        systemPrompt.setFont(Theme.regular(11));
        systemPrompt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_HARD, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        form.add(sp);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        JButton cancel = Components.secondaryButton("Cancel");
        cancel.addActionListener(e -> { saved = false; dispose(); });
        JButton save = Components.primaryButton("Save");
        save.addActionListener(e -> doSave());
        actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel field(String label, JTextField tf, String hint) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(Theme.medium(8));
        l.setForeground(Theme.TEXT_3);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        tf.setBackground(Theme.SURFACE_2);
        tf.setForeground(Theme.TEXT);
        tf.setCaretColor(Theme.ACCENT);
        tf.setFont(Theme.regular(11));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_HARD, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.add(tf);
        if (hint != null) {
            JLabel h = new JLabel(hint);
            h.setFont(Theme.regular(9));
            h.setForeground(Theme.TEXT_3);
            h.setAlignmentX(Component.LEFT_ALIGNMENT);
            h.setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 0));
            p.add(h);
        }
        return p;
    }

    private void doSave() {
        config.baseUrl = baseUrl.getText().trim();
        config.apiKey = new String(apiKey.getPassword()).trim();
        config.model = model.getText().trim();
        config.systemPrompt = systemPrompt.getText();
        if (config.baseUrl.isEmpty()) config.baseUrl = "https://api.openai.com/v1";
        if (config.model.isEmpty()) config.model = "gpt-4o-mini";
        config.save();
        saved = true;
        dispose();
    }
}
