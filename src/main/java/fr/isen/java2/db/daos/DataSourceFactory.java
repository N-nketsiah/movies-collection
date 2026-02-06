package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Factory class for database connections.
 * 
 * BONUS STAGE 1: Refactored to use DriverManager instead of SQLiteDataSource.
 * This makes the code database-agnostic - we can switch from SQLite to MySQL/PostgreSQL
 * just by changing the JDBC URL, without recompiling the code.
 * 
 * The database driver (SQLite, MySQL, etc.) is now a runtime dependency only.
 */
public class DataSourceFactory {
	
	// JDBC URL - change this to switch databases
	// SQLite: "jdbc:sqlite:sqlite.db"
	// MySQL: "jdbc:mysql://localhost:3306/database_name"
	// PostgreSQL: "jdbc:postgresql://localhost:5432/database_name"
	private static final String JDBC_URL = "jdbc:sqlite:sqlite.db";
	
	private DataSourceFactory() {
		// This is a static class that should not be instantiated.
		// Here's a way to remember it when this class will have 2K lines and you come
		// back to it in 2 years
		throw new IllegalStateException("This is a static class that should not be instantiated");
	}

	/**
	 * Returns a DataSource that provides database connections.
	 * 
	 * BONUS STAGE 1: This implementation wraps DriverManager in a DataSource
	 * to maintain compatibility with existing code while being driver-agnostic.
	 * 
	 * @return a DataSource for obtaining database connections
	 */
	public static DataSource getDataSource() {
		return new DataSource() {
			@Override
			public Connection getConnection() throws SQLException {
				// DriverManager automatically selects the right driver based on the URL
				// No explicit dependency on SQLiteDataSource!
				return DriverManager.getConnection(JDBC_URL);
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				return DriverManager.getConnection(JDBC_URL, username, password);
			}

			// The following methods are not used in this project
			// They are required by the DataSource interface
			
			@Override
			public java.io.PrintWriter getLogWriter() throws SQLException {
				throw new UnsupportedOperationException("Not implemented");
			}

			@Override
			public void setLogWriter(java.io.PrintWriter out) throws SQLException {
				throw new UnsupportedOperationException("Not implemented");
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
				DriverManager.setLoginTimeout(seconds);
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return DriverManager.getLoginTimeout();
			}

			@Override
			public java.util.logging.Logger getParentLogger() {
				throw new UnsupportedOperationException("Not implemented");
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new UnsupportedOperationException("Not implemented");
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}
		};
	}
}