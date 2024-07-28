package com.advancedfilescan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DirectoryScanner implements Runnable {
    private final File directory;
    private final File saveFile;
    private final ProgressListener progressListener;
    private final CompletionListener completionListener;

    public DirectoryScanner(File directory, File saveFile, ProgressListener progressListener, CompletionListener completionListener) {
        this.directory = directory;
        this.saveFile = saveFile;
        this.progressListener = progressListener;
        this.completionListener = completionListener;
    }

    @Override
    public void run() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("directory");
            rootElement.setAttribute("path", directory.getAbsolutePath());
            doc.appendChild(rootElement);

            AtomicInteger fileCount = new AtomicInteger();

            try (Stream<Path> paths = Files.walk(directory.toPath())) {
                long totalFiles = paths.filter(Files::isRegularFile).count();

                try (Stream<Path> filePathStream = Files.walk(directory.toPath())) {
                    filePathStream.filter(Files::isRegularFile).forEach(path -> {
                        fileCount.incrementAndGet();
                        Element fileElement = doc.createElement("file");
                        fileElement.setAttribute("name", path.getFileName().toString());
                        fileElement.setAttribute("path", path.toString().replace("\\", "\\\\"));
                        try {
                            long fileSize = Files.size(path);
                            fileElement.setAttribute("size", String.valueOf(fileSize));
                        } catch (IOException e) {
                            fileElement.setAttribute("size", "unknown");
                        }
                        rootElement.appendChild(fileElement);

                        int progress = (int) ((fileCount.get() / (double) totalFiles) * 100);
                        progressListener.onProgress(progress, "Scanning: " + path);
                    });
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(saveFile);
            transformer.transform(source, result);

            completionListener.onCompletion(saveFile, true);
        } catch (IOException | ParserConfigurationException | javax.xml.transform.TransformerException e) {
            progressListener.onProgress(0, "Error: " + e.getMessage());
            completionListener.onCompletion(saveFile, false);
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int progress, String message);
    }

    @FunctionalInterface
    public interface CompletionListener {
        void onCompletion(File resultFile, boolean success);
    }
}
