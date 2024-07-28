package com.advancedfilescan;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(0, 2));

        // Add various settings components here
        panel.add(new JLabel("Setting 1:"));
        panel.add(new JTextField());

        panel.add(new JLabel("Setting 2:"));
        panel.add(new JTextField());

        // ... Add more settings as needed

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSettings());

        add(panel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    private void saveSettings() {
        // Implement saving settings logic
        dispose();
    }
}
