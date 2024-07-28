package com.advancedfilescan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class AdvancedFileScan extends JFrame {
    private JTextField scanDirectoryField;
    private JTextField saveResultsField;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JButton scanButton;
    private JButton viewResultsButton;
    private File lastScanResult;

    public AdvancedFileScan() {
        setTitle("Advanced File Scan");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadResultsItem = new JMenuItem("Load Results XML");
        loadResultsItem.addActionListener(e -> loadResultsFile());
        fileMenu.add(loadResultsItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 3));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Scan Settings"));

        scanDirectoryField = new JTextField();
        JButton browseScanDirButton = new JButton("Browse...");
        browseScanDirButton.addActionListener(e -> chooseDirectory(scanDirectoryField));

        saveResultsField = new JTextField();
        JButton browseSaveFileButton = new JButton("Browse...");
        browseSaveFileButton.addActionListener(e -> chooseSaveFile(saveResultsField));

        scanButton = new JButton("Start Scan");
        scanButton.addActionListener(e -> startScan());

        inputPanel.add(new JLabel("Directory to Scan:"));
        inputPanel.add(scanDirectoryField);
        inputPanel.add(browseScanDirButton);
        inputPanel.add(new JLabel("Save Results to:"));
        inputPanel.add(saveResultsField);
        inputPanel.add(browseSaveFileButton);
        inputPanel.add(new JLabel(""));
        inputPanel.add(scanButton);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        viewResultsButton = new JButton("View Last Scan Results");
        viewResultsButton.setEnabled(false);
        viewResultsButton.addActionListener(e -> viewLastScanResults());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(logScrollPane, BorderLayout.CENTER);
        bottomPanel.add(viewResultsButton, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        add(panel);
    }

    private void chooseDirectory(JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            textField.setText(file.getAbsolutePath());
        }
    }

    private void chooseSaveFile(JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xml")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }
            textField.setText(file.getAbsolutePath());
        }
    }

    private void startScan() {
        String directoryPath = scanDirectoryField.getText();
        String saveFilePath = saveResultsField.getText();
        if (directoryPath.isEmpty() || saveFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify both directory to scan and file to save results.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "The specified directory to scan is not valid.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File saveFile = new File(saveFilePath);
        if (saveFile.isDirectory()) {
            JOptionPane.showMessageDialog(this, "The specified file to save results is not valid.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setValue(0);
        logArea.setText("");
        scanButton.setEnabled(false);
        viewResultsButton.setEnabled(false);

        DirectoryScanner scanner = new DirectoryScanner(directory, saveFile, this::onScanProgress, this::onScanComplete);
        new Thread(scanner).start();
    }

    private void onScanProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            logArea.append(message + "\n");
        });
    }

    private void onScanComplete(File resultFile, boolean success) {
        SwingUtilities.invokeLater(() -> {
            scanButton.setEnabled(true);
            if (success) {
                lastScanResult = resultFile;
                viewResultsButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Scan completed successfully!",
                        "Scan Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Scan failed. Check log for details.",
                        "Scan Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void viewLastScanResults() {
        if (lastScanResult != null) {
            new ScanResultViewer(lastScanResult).setVisible(true);
        }
    }

    private void loadResultsFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.getName().toLowerCase().endsWith(".xml")) {
                new ScanResultViewer(file).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a valid XML file.",
                        "Invalid File", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Set the look and feel to the system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new AdvancedFileScan().setVisible(true));
    }
}
