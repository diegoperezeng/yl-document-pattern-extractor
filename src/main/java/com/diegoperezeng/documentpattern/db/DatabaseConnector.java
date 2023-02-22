package com.diegoperezeng.documentpattern.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnector {

  private Connection conn;

  private static final Logger logger = LoggerFactory.getLogger(
    DatabaseConnector.class
  );

  private static String SQL_EXCEPTION = "Error executing SQL query:";

  public DatabaseConnector(String url, String username, String password)
    throws SQLException {
    conn = DriverManager.getConnection(url, username, password);
  }

  public ResultSet executeQuery(String query) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      logger.error(SQL_EXCEPTION + e.getMessage(), e);
    }
    return null;
  }

  public void executeUpdate(String query) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      logger.error(SQL_EXCEPTION + e.getMessage(), e);
    }
  }
}
