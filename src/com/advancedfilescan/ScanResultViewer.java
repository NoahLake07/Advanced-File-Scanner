package com.advancedfilescan;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScanResultViewer extends JFrame {
    private JTree fileTree;
    private JPopupMenu contextMenu;

    public ScanResultViewer(File scanResultFile) {
        setTitle("Scan Results");
        setSize(600, 400);
        setLocationRelativeTo(null);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(scanResultFile.getName());
        fileTree = new JTree(rootNode);
        loadScanResults(scanResultFile, rootNode);
        expandAllNodes(fileTree, 0, fileTree.getRowCount());

        contextMenu = new JPopupMenu();
        JMenuItem openFileItem = new JMenuItem("Open File");
        JMenuItem openLocationItem = new JMenuItem("Open File Location");
        JMenuItem viewPropertiesItem = new JMenuItem("View Properties");

        contextMenu.add(openFileItem);
        contextMenu.add(openLocationItem);
        contextMenu.add(viewPropertiesItem);

        openFileItem.addActionListener(new OpenFileActionListener());
        openLocationItem.addActionListener(new OpenLocationActionListener());
        viewPropertiesItem.addActionListener(new ViewPropertiesActionListener());

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
                    fileTree.setSelectionRow(row);
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileTree);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadScanResults(File scanResultFile, DefaultMutableTreeNode rootNode) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(scanResultFile);

            NodeList nodeList = doc.getElementsByTagName("file");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String filePath = node.getAttributes().getNamedItem("path").getNodeValue().replace("\\\\", "\\");
                String fileName = node.getAttributes().getNamedItem("name").getNodeValue();
                String fileSize = node.getAttributes().getNamedItem("size").getNodeValue();

                FileData fileData = new FileData(fileName, filePath, fileSize);
                addFileToTree(rootNode, fileData);
            }

            DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
            model.reload();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading scan results: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFileToTree(DefaultMutableTreeNode rootNode, FileData fileData) {
        String[] pathParts = fileData.getPath().split(File.separator.equals("\\") ? "\\\\" : File.separator);
        DefaultMutableTreeNode currentNode = rootNode;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            DefaultMutableTreeNode childNode = findChildNode(currentNode, part);
            if (childNode == null) {
                childNode = new DefaultMutableTreeNode(part);
                currentNode.add(childNode);
            }
            currentNode = childNode;
        }

        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileData);
        currentNode.add(fileNode);
    }

    private DefaultMutableTreeNode findChildNode(DefaultMutableTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (name.equals(child.getUserObject().toString())) {
                return child;
            }
        }
        return null;
    }

    private class OpenFileActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileData fileData = getSelectedFileData();
            if (fileData != null) {
                try {
                    Desktop.getDesktop().open(new File(fileData.getPath()));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ScanResultViewer.this, "Unable to open file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class OpenLocationActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileData fileData = getSelectedFileData();
            if (fileData != null) {
                try {
                    Desktop.getDesktop().open(new File(fileData.getPath()).getParentFile());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ScanResultViewer.this, "Unable to open file location: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ViewPropertiesActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileData fileData = getSelectedFileData();
            if (fileData != null) {
                JOptionPane.showMessageDialog(ScanResultViewer.this,
                        "File: " + fileData.getName() + "\nPath: " + fileData.getPath() + "\nSize: " + fileData.getSize() + " bytes",
                        "File Properties", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private FileData getSelectedFileData() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof FileData) {
            return (FileData) selectedNode.getUserObject();
        }
        return null;
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            File dummyFile = new File("dummy.xml");
            new ScanResultViewer(dummyFile).setVisible(true);
        });
    }
}

class FileData {
    private final String name;
    private final String path;
    private final String size;

    public FileData(String name, String path, String size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String toString() {
        return name;
    }
}
