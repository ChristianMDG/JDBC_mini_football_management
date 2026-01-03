package org.mini_football_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private  String JDBC_URl = System.getenv("url");
    private  String USERNAME = System.getenv("username");
    private  String PASSWORD = System.getenv("password");

    public Connection getConnection() throws SQLException {
        if (JDBC_URl == null || USERNAME == null || PASSWORD == null) {
            throw new RuntimeException("Connection Error");
        }
        System.out.println("Connected");
        return DriverManager.getConnection(JDBC_URl, USERNAME, PASSWORD);
    }

}
