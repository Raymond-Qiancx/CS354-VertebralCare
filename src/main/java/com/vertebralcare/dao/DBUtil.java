package com.vertebralcare.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 */
public class DBUtil {

    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
            } else {
                // Default configuration
                url = "jdbc:mysql://localhost:3306/vertebral_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
                user = "root";
                password = "";
            }

            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            initialized = true;

        } catch (IOException e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database not initialized properly");
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 关闭资源
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore close exceptions
                }
            }
        }
    }

    /**
     * 更新数据库配置（用于运行时修改）
     */
    public static void updateConfig(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
