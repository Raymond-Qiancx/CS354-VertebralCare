package com.vertebralcare.ui;

import com.vertebralcare.model.DiagnosisClass;
import com.vertebralcare.model.Exam;
import com.vertebralcare.model.Patient;
import com.vertebralcare.service.ExamService;
import com.vertebralcare.service.PatientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Exam Records Management Panel
 */
public class ExamPanel extends JPanel {

    private final ExamService examService;
    private final PatientService patientService;

    private JComboBox<PatientItem> patientCombo;
    private JComboBox<ClassItem> classFilterCombo;
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton, viewButton;

    private static final String[] COLUMN_NAMES = {
            "ID", "Patient", "Exam Date", "Pelvic Incidence", "Pelvic Tilt", "Lumbar Lordosis",
            "Sacral Slope", "Pelvic Radius", "Spondylolisthesis", "Diagnosis"
    };

    public ExamPanel(ExamService examService, PatientService patientService) {
        this.examService = examService;
        this.patientService = patientService;
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        // Patient filter dropdown
        patientCombo = new JComboBox<>();
        patientCombo.addItem(new PatientItem(0, "All Patients"));
        loadPatients();
        patientCombo.addActionListener(e -> filterByPatient());

        // Diagnosis class filter
        classFilterCombo = new JComboBox<>();
        classFilterCombo.addItem(new ClassItem(0, "All Classes"));
        loadClasses();
        classFilterCombo.addActionListener(e -> filterByClass());

        // Buttons
        addButton = new JButton("Add Exam");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        viewButton = new JButton("View Details");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteExam());
        viewButton.addActionListener(e -> showDetailDialog());
        refreshButton.addActionListener(e -> refreshData());

        // Table
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        examTable = new JTable(tableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.setRowHeight(25);
        examTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Double click to view details
        examTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            for (Patient p : patients) {
                patientCombo.addItem(new PatientItem(p.getPatientId(), p.getName()));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void loadClasses() {
        try {
            List<DiagnosisClass> classes = examService.getAllDiagnosisClasses();
            for (DiagnosisClass dc : classes) {
                classFilterCombo.addItem(new ClassItem(dc.getClassId(), dc.getDescription()));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.add(new JLabel("Filter by Patient:"));
        toolBar.add(patientCombo);
        toolBar.add(new JLabel("Filter by Class:"));
        toolBar.add(classFilterCombo);
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(addButton);
        toolBar.add(viewButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        // Table area
        JScrollPane scrollPane = new JScrollPane(examTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom hint
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JLabel("Tip: Double-click a row to view exam details"));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            List<Exam> exams = examService.getAllExams();
            updateTable(exams);
        } catch (Exception e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }

    private void filterByPatient() {
        PatientItem selected = (PatientItem) patientCombo.getSelectedItem();
        if (selected == null || selected.id == 0) {
            loadData();
            return;
        }

        try {
            List<Exam> exams = examService.getExamsByPatientId(selected.id);
            updateTable(exams);
        } catch (Exception e) {
            showError("Filter failed: " + e.getMessage());
        }
    }

    private void filterByClass() {
        ClassItem selected = (ClassItem) classFilterCombo.getSelectedItem();
        if (selected == null || selected.id == 0) {
            loadData();
            return;
        }

        try {
            List<Exam> exams = examService.getExamsByClassId(selected.id);
            updateTable(exams);
        } catch (Exception e) {
            showError("Filter failed: " + e.getMessage());
        }
    }

    private void updateTable(List<Exam> exams) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Exam e : exams) {
            tableModel.addRow(new Object[]{
                    e.getExamId(),
                    e.getPatientName(),
                    e.getExamDate() != null ? sdf.format(e.getExamDate()) : "-",
                    String.format("%.2f", e.getPelvicIncidence()),
                    String.format("%.2f", e.getPelvicTilt()),
                    String.format("%.2f", e.getLumbarLordosisAngle()),
                    String.format("%.2f", e.getSacralSlope()),
                    String.format("%.2f", e.getPelvicRadius()),
                    String.format("%.2f", e.getDegreeSpondylolisthesis()),
                    e.getDiagnosisDescription()
            });
        }
    }

    private void showAddDialog() {
        ExamDialog dialog = new ExamDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add Exam Record",
                null,
                patientService,
                examService
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Exam exam = dialog.getExam();
                int id = examService.addExam(exam);
                if (id > 0) {
                    showMessage("Exam record added successfully");
                    refreshData();
                }
            } catch (Exception e) {
                showError("Failed to add: " + e.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int row = examTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a record to edit");
            return;
        }

        int examId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Exam exam = examService.getExamById(examId);
            if (exam != null) {
                ExamDialog dialog = new ExamDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        "Edit Exam Record",
                        exam,
                        patientService,
                        examService
                );
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Exam updated = dialog.getExam();
                    updated.setExamId(examId);
                    if (examService.updateExam(updated)) {
                        showMessage("Exam record updated successfully");
                        refreshData();
                    }
                }
            }
        } catch (Exception e) {
            showError("Edit failed: " + e.getMessage());
        }
    }

    private void showDetailDialog() {
        int row = examTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a record to view");
            return;
        }

        int examId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Exam exam = examService.getExamById(examId);
            if (exam != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exam ID: ").append(exam.getExamId()).append("\n");
                sb.append("Patient: ").append(exam.getPatientName()).append("\n");
                sb.append("Exam Date: ").append(exam.getExamDate()).append("\n\n");
                sb.append("=== Biomechanical Indicators ===\n");
                sb.append("Pelvic Incidence (PI): ").append(String.format("%.4f", exam.getPelvicIncidence())).append("\n");
                sb.append("Pelvic Tilt (PT): ").append(String.format("%.4f", exam.getPelvicTilt())).append("\n");
                sb.append("Lumbar Lordosis Angle (LL): ").append(String.format("%.4f", exam.getLumbarLordosisAngle())).append("\n");
                sb.append("Sacral Slope (SS): ").append(String.format("%.4f", exam.getSacralSlope())).append("\n");
                sb.append("Pelvic Radius (PR): ").append(String.format("%.4f", exam.getPelvicRadius())).append("\n");
                sb.append("Degree of Spondylolisthesis (GS): ").append(String.format("%.4f", exam.getDegreeSpondylolisthesis())).append("\n\n");
                sb.append("=== Diagnosis Result ===\n");
                sb.append(exam.getDiagnosisDescription()).append("\n");
                if (exam.getNotes() != null && !exam.getNotes().isEmpty()) {
                    sb.append("\nNotes: ").append(exam.getNotes());
                }

                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 350));

                JOptionPane.showMessageDialog(this, scrollPane, "Exam Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            showError("Failed to get details: " + e.getMessage());
        }
    }

    private void deleteExam() {
        int row = examTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a record to delete");
            return;
        }

        int examId = (Integer) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this exam record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (examService.deleteExam(examId)) {
                    showMessage("Deleted successfully");
                    refreshData();
                }
            } catch (Exception e) {
                showError("Delete failed: " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        // Refresh patient dropdown
        patientCombo.removeAllItems();
        patientCombo.addItem(new PatientItem(0, "All Patients"));
        loadPatients();

        loadData();
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper classes
    private static class PatientItem {
        int id;
        String name;

        PatientItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id == 0 ? name : id + " - " + name;
        }
    }

    private static class ClassItem {
        int id;
        String name;

        ClassItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Exam Record Edit Dialog
     */
    private static class ExamDialog extends JDialog {
        private JComboBox<PatientItem> patientCombo;
        private JTextField examDateField;
        private JTextField piField, ptField, llField, ssField, prField, gsField;
        private JComboBox<ClassItem> classCombo;
        private JTextArea notesArea;
        private boolean confirmed = false;

        public ExamDialog(Frame owner, String title, Exam exam,
                          PatientService patientService, ExamService examService) {
            super(owner, title, true);
            initComponents(exam, patientService, examService);
            pack();
            setLocationRelativeTo(owner);
        }

        private void initComponents(Exam exam, PatientService patientService, ExamService examService) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // Patient selection
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Patient*:"), gbc);
            gbc.gridx = 1;
            patientCombo = new JComboBox<>();
            try {
                for (Patient p : patientService.getAllPatients()) {
                    patientCombo.addItem(new PatientItem(p.getPatientId(), p.getName()));
                }
            } catch (Exception e) { }
            panel.add(patientCombo, gbc);

            // Exam date
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Exam Date*:"), gbc);
            gbc.gridx = 1;
            examDateField = new JTextField(15);
            examDateField.setToolTipText("Format: yyyy-MM-dd");
            if (exam == null) {
                examDateField.setText(new java.sql.Date(System.currentTimeMillis()).toString());
            }
            panel.add(examDateField, gbc);

            // 6 indicators
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Pelvic Incidence (PI):"), gbc);
            gbc.gridx = 1;
            piField = new JTextField(15);
            panel.add(piField, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Pelvic Tilt (PT):"), gbc);
            gbc.gridx = 1;
            ptField = new JTextField(15);
            panel.add(ptField, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Lumbar Lordosis Angle (LL):"), gbc);
            gbc.gridx = 1;
            llField = new JTextField(15);
            panel.add(llField, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Sacral Slope (SS):"), gbc);
            gbc.gridx = 1;
            ssField = new JTextField(15);
            panel.add(ssField, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Pelvic Radius (PR):"), gbc);
            gbc.gridx = 1;
            prField = new JTextField(15);
            panel.add(prField, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Spondylolisthesis (GS):"), gbc);
            gbc.gridx = 1;
            gsField = new JTextField(15);
            panel.add(gsField, gbc);

            // Diagnosis class
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Diagnosis*:"), gbc);
            gbc.gridx = 1;
            classCombo = new JComboBox<>();
            try {
                for (DiagnosisClass dc : examService.getAllDiagnosisClasses()) {
                    classCombo.addItem(new ClassItem(dc.getClassId(), dc.getDescription()));
                }
            } catch (Exception e) { }
            panel.add(classCombo, gbc);

            // Notes
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            notesArea = new JTextArea(3, 15);
            panel.add(new JScrollPane(notesArea), gbc);

            // If editing, fill in data
            if (exam != null) {
                // Select corresponding patient
                for (int i = 0; i < patientCombo.getItemCount(); i++) {
                    if (patientCombo.getItemAt(i).id == exam.getPatientId()) {
                        patientCombo.setSelectedIndex(i);
                        break;
                    }
                }
                if (exam.getExamDate() != null) {
                    examDateField.setText(exam.getExamDate().toString());
                }
                piField.setText(String.valueOf(exam.getPelvicIncidence()));
                ptField.setText(String.valueOf(exam.getPelvicTilt()));
                llField.setText(String.valueOf(exam.getLumbarLordosisAngle()));
                ssField.setText(String.valueOf(exam.getSacralSlope()));
                prField.setText(String.valueOf(exam.getPelvicRadius()));
                gsField.setText(String.valueOf(exam.getDegreeSpondylolisthesis()));
                // Select corresponding diagnosis class
                for (int i = 0; i < classCombo.getItemCount(); i++) {
                    if (classCombo.getItemAt(i).id == exam.getClassId()) {
                        classCombo.setSelectedIndex(i);
                        break;
                    }
                }
                if (exam.getNotes() != null) {
                    notesArea.setText(exam.getNotes());
                }
            }

            // Buttons
            row++;
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");

            okButton.addActionListener(e -> {
                if (validateInput()) {
                    confirmed = true;
                    dispose();
                }
            });
            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0; gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(buttonPanel, gbc);

            setContentPane(new JScrollPane(panel));
            setPreferredSize(new Dimension(450, 500));
        }

        private boolean validateInput() {
            if (patientCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a patient", "Validation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (examDateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Exam date cannot be empty", "Validation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Exam getExam() {
            Exam exam = new Exam();

            PatientItem pi = (PatientItem) patientCombo.getSelectedItem();
            exam.setPatientId(pi.id);

            try {
                exam.setExamDate(Date.valueOf(examDateField.getText().trim()));
            } catch (Exception e) {
                exam.setExamDate(new Date(System.currentTimeMillis()));
            }

            exam.setPelvicIncidence(parseDouble(piField.getText()));
            exam.setPelvicTilt(parseDouble(ptField.getText()));
            exam.setLumbarLordosisAngle(parseDouble(llField.getText()));
            exam.setSacralSlope(parseDouble(ssField.getText()));
            exam.setPelvicRadius(parseDouble(prField.getText()));
            exam.setDegreeSpondylolisthesis(parseDouble(gsField.getText()));

            ClassItem ci = (ClassItem) classCombo.getSelectedItem();
            exam.setClassId(ci.id);

            exam.setNotes(notesArea.getText().trim());

            return exam;
        }

        private double parseDouble(String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
    }
}
