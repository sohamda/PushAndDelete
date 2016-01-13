package com.push.and.delete.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

public class DBConnectionFactory {
    
    public DBConnectionFactory() {
        super();
    }
    
    protected static Connection conn = null;

    public static Connection getConnection() throws Exception {
        if (conn == null) {
            try {
                // create a database connection
                String Dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
                String connStr = "jdbc:sqlite:" + Dir + "/localData.db";
                Class.forName("SQLite.JDBCDriver");
                conn = DriverManager.getConnection(connStr);

            } catch (SQLException e) {
                // if the error message is "out of memory",
                // it probably means no database file is found
                System.err.println(e.getClass().getName() + ": " + e.getMessage() );
                e.printStackTrace();
            }
        }
        return conn;
    }
}
