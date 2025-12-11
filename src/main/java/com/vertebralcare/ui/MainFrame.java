package com.vertebralcare.ui;

import com.vertebralcare.dao.DBUtil;
import com.vertebralcare.service.ExamService;
import com.vertebralcare.service.ImportService;
import com.vertebralcare.service.PatientService;
import com.vertebralcare.service.StatisticsService;

import javax.swing.*;
import java.awt.*;

/**
 * Main Window
 */
public class MainFrame extends JFrame {

    private final PatientService patientService;
    private final ExamService examService;
    private final StatisticsService statisticsService;
    private final ImportService importService;

    private JTabbedPane tabbedPane;
    private PatientPanel patientPanel;
    private ExamPanel examPanel;
    private StatisticsPanel statisticsPanel;
    private ImportPanel importPanel;
    private JLabel statusLabel;

    public MainFrame() {
        this.patientService = new PatientService();
        this.examService = new ExamService();
        this.statisticsService = new StatisticsService();
        this.importService = new ImportService();

        initComponents();
        setupLayout();
        configureFrame();
    }

    private void initComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Create panels
        patientPanel = new PatientPanel(patientService);
        examPanel = new ExamPanel(examService, patientService);
        statisticsPanel = new StatisticsPanel(statisticsService);
        importPanel = new ImportPanel(importService, this::refreshAllPanels);

        // Add to tabs
        tabbedPane.addTab("Patient Management", createIcon("patient"), patientPanel);
        tabbedPane.addTab("Exam Records", createIcon("exam"), examPanel);
        tabbedPane.addTab("Statistics", createIcon("stats"), statisticsPanel);
        tabbedPane.addTab("Data Import", createIcon("import"), importPanel);

        // Refresh data on tab switch
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            switch (index) {
                case 0: patientPanel.refreshData(); break;
                case 1: examPanel.refreshData(); break;
                case 2: statisticsPanel.refreshData(); break;
            }
        });

        // Status bar
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        updateStatus();
    }

    private ImageIcon createIcon(String name) {
        // Return null, can add icons later
        return null;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("VertebralCare - Spine Case Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Database status indicator
        JLabel dbLabel = new JLabel(DBUtil.testConnection() ? "● Database Connected" : "○ Database Disconnected");
        dbLabel.setForeground(Color.WHITE);
        dbLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        headerPanel.add(dbLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content area
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void configureFrame() {
        setTitle("VertebralCare - Spine Case Management & Risk Assessment System");
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Use default look and feel
        }
    }

    private void updateStatus() {
        try {
            int patientCount = patientService.getPatientCount();
            int examCount = examService.getExamCount();
            statusLabel.setText(String.format("Patients: %d | Exam Records: %d", patientCount, examCount));
        } catch (Exception e) {
            statusLabel.setText("Failed to get status");
        }
    }

    public void refreshAllPanels() {
        patientPanel.refreshData();
        examPanel.refreshData();
        statisticsPanel.refreshData();
        updateStatus();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
