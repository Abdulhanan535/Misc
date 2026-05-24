package com.pcbuildstore.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pc_build_store?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    private static DBConnection instance;
    private Connection conn;

    private DBConnection() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DBConnection get() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection connection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
