package com.vertebralcare.ui;

import com.vertebralcare.service.StatisticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Statistics Analysis Panel
 */
public class StatisticsPanel extends JPanel {

    private final StatisticsService statisticsService;

    private JLabel patientCountLabel;
    private JLabel examCountLabel;
    private JLabel normalPercentLabel;
    private JLabel abnormalPercentLabel;
    private JPanel chartPanel;
    private JTable avgTable;
    private DefaultTableModel avgTableModel;

    public StatisticsPanel(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        // Statistics labels
        patientCountLabel = new JLabel("Patients: -");
        examCountLabel = new JLabel("Exams: -");
        normalPercentLabel = new JLabel("Normal: -");
        abnormalPercentLabel = new JLabel("Abnormal: -");

        Font statFont = new Font("SansSerif", Font.BOLD, 16);
        patientCountLabel.setFont(statFont);
        examCountLabel.setFont(statFont);
        normalPercentLabel.setFont(statFont);
        abnormalPercentLabel.setFont(statFont);

        // Chart panel
        chartPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Average table
        String[] columns = {"Indicator", "Normal", "Abnormal"};
        avgTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        avgTable = new JTable(avgTableModel);
        avgTable.setRowHeight(28);
        avgTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 20, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Statistics Overview"));

        summaryPanel.add(createStatCard(patientCountLabel, new Color(70, 130, 180)));
        summaryPanel.add(createStatCard(examCountLabel, new Color(60, 179, 113)));
        summaryPanel.add(createStatCard(normalPercentLabel, new Color(46, 139, 87)));
        summaryPanel.add(createStatCard(abnormalPercentLabel, new Color(205, 92, 92)));

        add(summaryPanel, BorderLayout.NORTH);

        // Center chart area
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Charts
        chartPanel.setBorder(BorderFactory.createTitledBorder("Diagnosis Distribution"));
        chartPanel.setPreferredSize(new Dimension(800, 300));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom average table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Average Indicator Values by Class"));
        tablePanel.add(new JScrollPane(avgTable), BorderLayout.CENTER);
        tablePanel.setPreferredSize(new Dimension(800, 200));

        add(tablePanel, BorderLayout.SOUTH);

        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        ((JPanel) summaryPanel.getParent()).add(btnPanel, BorderLayout.EAST);
    }

    private JPanel createStatCard(JLabel label, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        try {
            // Basic statistics
            int patientCount = statisticsService.getPatientCount();
            int examCount = statisticsService.getExamCount();
            double normalPercent = statisticsService.getNormalPercentage();
            double abnormalPercent = statisticsService.getAbnormalPercentage();

            patientCountLabel.setText("Patients: " + patientCount);
            examCountLabel.setText("Exams: " + examCount);
            normalPercentLabel.setText(String.format("Normal: %.1f%%", normalPercent));
            abnormalPercentLabel.setText(String.format("Abnormal: %.1f%%", abnormalPercent));

            // Class statistics
            Map<String, Integer> countByClass = statisticsService.getCountByClass();

            // Update charts
            updateCharts(countByClass);

            // Average table
            updateAverageTable();

        } catch (Exception e) {
            showError("Failed to load statistics: " + e.getMessage());
        }
    }

    private void updateCharts(Map<String, Integer> countByClass) {
        chartPanel.removeAll();

        // Pie chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : countByClass.entrySet()) {
            pieDataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Diagnosis Class Distribution",
                pieDataset,
                true,
                true,
                false
        );

        // Set pie chart colors
        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setSectionPaint("Normal", new Color(46, 139, 87));
        piePlot.setSectionPaint("Abnormal", new Color(205, 92, 92));

        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        chartPanel.add(pieChartPanel);

        // Bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : countByClass.entrySet()) {
            barDataset.addValue(entry.getValue(), "Cases", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Cases by Diagnosis Class",
                "Diagnosis Class",
                "Count",
                barDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        CategoryPlot categoryPlot = barChart.getCategoryPlot();
        categoryPlot.getRenderer().setSeriesPaint(0, new Color(70, 130, 180));

        ChartPanel barChartPanel = new ChartPanel(barChart);
        chartPanel.add(barChartPanel);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateAverageTable() {
        avgTableModel.setRowCount(0);

        try {
            Map<String, double[]> averages = statisticsService.getAveragesByClass();
            String[] indicatorNames = StatisticsService.getIndicatorNames();

            // Get Normal and Abnormal data
            double[] normalAvgs = averages.get("Normal");
            double[] abnormalAvgs = averages.get("Abnormal");

            for (int i = 0; i < indicatorNames.length; i++) {
                String normalVal = normalAvgs != null ? String.format("%.4f", normalAvgs[i]) : "-";
                String abnormalVal = abnormalAvgs != null ? String.format("%.4f", abnormalAvgs[i]) : "-";
                avgTableModel.addRow(new Object[]{indicatorNames[i], normalVal, abnormalVal});
            }

        } catch (Exception e) {
            // Ignore errors
        }
    }

    public void refreshData() {
        loadData();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
