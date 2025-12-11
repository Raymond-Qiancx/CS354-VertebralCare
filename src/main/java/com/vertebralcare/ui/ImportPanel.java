package com.vertebralcare.ui;

import com.vertebralcare.service.ImportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Data Import Panel
 */
public class ImportPanel extends JPanel {

    private final ImportService importService;
    private final Runnable refreshCallback;

    private JTextField filePathField;
    private JButton browseButton;
    private JButton importButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JLabel statusLabel;

    public ImportPanel(ImportService importService, Runnable refreshCallback) {
        this.importService = importService;
        this.refreshCallback = refreshCallback;
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        // File path
        filePathField = new JTextField(40);
        filePathField.setEditable(false);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseFile());

        importButton = new JButton("Start Import");
        importButton.addActionListener(e -> startImport());
        importButton.setEnabled(false);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Waiting for import...");

        // Log area
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        statusLabel = new JLabel("Please select a CSV file to import");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top instructions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("CSV Data Import"));

        JTextArea helpText = new JTextArea(
                "Instructions:\n" +
                "1. Select a CSV file in the correct format (Vertebral Column dataset)\n" +
                "2. CSV format: Pelvic Incidence, Pelvic Tilt, Lumbar Lordosis Angle, Sacral Slope, Pelvic Radius, Spondylolisthesis Degree, Class (Normal/Abnormal)\n" +
                "3. Virtual patient info will be automatically generated during import\n" +
                "4. Each CSV record will create one patient and one exam record"
        );
        helpText.setEditable(false);
        helpText.setBackground(new Color(255, 255, 224));
        helpText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(helpText, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Center control area
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // File selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filePanel.setBorder(BorderFactory.createTitledBorder("Select File"));
        filePanel.add(new JLabel("CSV File:"));
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        filePanel.add(Box.createHorizontalStrut(20));
        filePanel.add(importButton);

        centerPanel.add(filePanel, BorderLayout.NORTH);

        // Progress
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Import Progress"));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);

        centerPanel.add(progressPanel, BorderLayout.CENTER);

        // Log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Import Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        JPanel logBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logBtnPanel.add(clearLogBtn);
        logPanel.add(logBtnPanel, BorderLayout.SOUTH);

        centerPanel.add(logPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
            importButton.setEnabled(true);
            log("File selected: " + file.getName());
        }
    }

    private void startImport() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            showError("Please select a CSV file first");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            showError("File does not exist");
            return;
        }

        // Disable buttons
        importButton.setEnabled(false);
        browseButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Importing...");

        log("Starting import: " + file.getName());
        log("-----------------------------------");

        // Execute import in background thread
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return importService.importCSV(file, (current, total) -> {
                    int percent = (int) ((current * 100.0) / total);
                    publish(percent);
                    setProgress(percent);
                });
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                progressBar.setValue(latest);
                progressBar.setString(latest + "%");
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    log("-----------------------------------");
                    log("Import completed! Total " + count + " records imported");
                    progressBar.setValue(100);
                    progressBar.setString("Completed");
                    statusLabel.setText("Import completed: " + count + " records");

                    showMessage("Import successful! " + count + " exam records imported");

                    // Refresh other panels
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }

                } catch (Exception e) {
                    log("Import failed: " + e.getMessage());
                    progressBar.setString("Failed");
                    statusLabel.setText("Import failed");
                    showError("Import failed: " + e.getMessage());
                } finally {
                    importButton.setEnabled(true);
                    browseButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void log(String message) {
        logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
