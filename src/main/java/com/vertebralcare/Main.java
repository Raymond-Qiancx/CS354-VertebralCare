package com.vertebralcare;

import com.vertebralcare.dao.DBUtil;
import com.vertebralcare.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * VertebralCare - Spine Case Management System
 * Application Entry Point
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        try {
            // Use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Set font
            Font font = new Font("SansSerif", Font.PLAIN, 13);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("TableHeader.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("OptionPane.messageFont", font);
            UIManager.put("OptionPane.buttonFont", font);

        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }

        // Check database connection
        System.out.println("VertebralCare - Spine Case Management & Risk Assessment System");
        System.out.println("=============================================================");
        System.out.println("Checking database connection...");

        boolean dbConnected = DBUtil.testConnection();
        if (dbConnected) {
            System.out.println("Database connection successful!");
        } else {
            System.err.println("Warning: Database connection failed! Please check:");
            System.err.println("  1. Is MySQL service running?");
            System.err.println("  2. Is database 'vertebral_db' created?");
            System.err.println("  3. Is db.properties configured correctly?");
            System.err.println("");
            System.err.println("Please run init.sql script to initialize the database.");
        }

        // Start GUI in Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);

                // Show warning if database not connected
                if (!dbConnected) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Database connection failed!\n\n" +
                                    "Please ensure:\n" +
                                    "1. MySQL service is running\n" +
                                    "2. init.sql has been executed to create database\n" +
                                    "3. db.properties is configured correctly\n\n" +
                                    "Some features may not work.",
                            "Database Connection Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Application startup failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}
