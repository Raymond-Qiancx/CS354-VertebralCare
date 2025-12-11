package com.vertebralcare.ui;

import com.vertebralcare.model.Patient;
import com.vertebralcare.service.PatientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Patient Management Panel
 */
public class PatientPanel extends JPanel {

    private final PatientService patientService;

    private JTextField searchField;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    private static final String[] COLUMN_NAMES = {"ID", "Name", "Gender", "Birth Date", "Phone", "Created At"};

    public PatientPanel(PatientService patientService) {
        this.patientService = patientService;
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        // Search field
        searchField = new JTextField(20);
        searchField.addActionListener(e -> searchPatients());

        // Buttons
        addButton = new JButton("Add Patient");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deletePatient());
        refreshButton.addActionListener(e -> refreshData());

        // Table
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(25);
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Double click to edit
        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditDialog();
                }
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.add(new JLabel("Search:"));
        toolBar.add(searchField);
        toolBar.add(new JButton("Search") {{
            addActionListener(e -> searchPatients());
        }});
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        // Table area
        JScrollPane scrollPane = new JScrollPane(patientTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom hint
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JLabel("Tip: Double-click a row to edit patient info"));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            updateTable(patients);
        } catch (Exception e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }

    private void searchPatients() {
        try {
            String keyword = searchField.getText().trim();
            List<Patient> patients = patientService.searchByName(keyword);
            updateTable(patients);
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void updateTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getGenderDisplay(),
                    p.getBirthDate() != null ? sdf.format(p.getBirthDate()) : "-",
                    p.getPhone() != null ? p.getPhone() : "-",
                    p.getCreatedAt() != null ? sdf2.format(p.getCreatedAt()) : "-"
            });
        }
    }

    private void showAddDialog() {
        PatientDialog dialog = new PatientDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Patient", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Patient patient = dialog.getPatient();
                int id = patientService.addPatient(patient);
                if (id > 0) {
                    showMessage("Patient added successfully, ID: " + id);
                    refreshData();
                }
            } catch (Exception e) {
                showError("Failed to add: " + e.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a patient to edit");
            return;
        }

        int patientId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Patient patient = patientService.getPatientById(patientId);
            if (patient != null) {
                PatientDialog dialog = new PatientDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        "Edit Patient",
                        patient
                );
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Patient updated = dialog.getPatient();
                    updated.setPatientId(patientId);
                    if (patientService.updatePatient(updated)) {
                        showMessage("Patient info updated successfully");
                        refreshData();
                    }
                }
            }
        } catch (Exception e) {
            showError("Edit failed: " + e.getMessage());
        }
    }

    private void deletePatient() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a patient to delete");
            return;
        }

        int patientId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete patient \"" + name + "\" and all their exam records?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (patientService.deletePatient(patientId)) {
                    showMessage("Deleted successfully");
                    refreshData();
                }
            } catch (Exception e) {
                showError("Delete failed: " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        loadData();
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Patient Edit Dialog
     */
    private static class PatientDialog extends JDialog {
        private JTextField nameField;
        private JComboBox<String> genderCombo;
        private JTextField birthDateField;
        private JTextField phoneField;
        private boolean confirmed = false;

        public PatientDialog(Frame owner, String title, Patient patient) {
            super(owner, title, true);
            initComponents(patient);
            pack();
            setLocationRelativeTo(owner);
        }

        private void initComponents(Patient patient) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Name
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Name*:"), gbc);
            gbc.gridx = 1;
            nameField = new JTextField(20);
            panel.add(nameField, gbc);

            // Gender
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Gender:"), gbc);
            gbc.gridx = 1;
            genderCombo = new JComboBox<>(new String[]{"", "Male", "Female"});
            panel.add(genderCombo, gbc);

            // Birth Date
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Birth Date:"), gbc);
            gbc.gridx = 1;
            birthDateField = new JTextField(20);
            birthDateField.setToolTipText("Format: yyyy-MM-dd");
            panel.add(birthDateField, gbc);

            // Phone
            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            phoneField = new JTextField(20);
            panel.add(phoneField, gbc);

            // If editing, fill in data
            if (patient != null) {
                nameField.setText(patient.getName());
                if ("M".equals(patient.getGender())) {
                    genderCombo.setSelectedItem("Male");
                } else if ("F".equals(patient.getGender())) {
                    genderCombo.setSelectedItem("Female");
                }
                if (patient.getBirthDate() != null) {
                    birthDateField.setText(patient.getBirthDate().toString());
                }
                if (patient.getPhone() != null) {
                    phoneField.setText(patient.getPhone());
                }
            }

            // Buttons
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

            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(buttonPanel, gbc);

            setContentPane(panel);
        }

        private boolean validateInput() {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty", "Validation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Patient getPatient() {
            Patient patient = new Patient();
            patient.setName(nameField.getText().trim());

            String gender = (String) genderCombo.getSelectedItem();
            if ("Male".equals(gender)) {
                patient.setGender("M");
            } else if ("Female".equals(gender)) {
                patient.setGender("F");
            }

            String birthStr = birthDateField.getText().trim();
            if (!birthStr.isEmpty()) {
                try {
                    patient.setBirthDate(Date.valueOf(birthStr));
                } catch (Exception e) {
                    // Ignore date parsing error
                }
            }

            patient.setPhone(phoneField.getText().trim());
            return patient;
        }
    }
}
