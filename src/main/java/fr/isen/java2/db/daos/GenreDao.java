package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;

/**
 * DAO (Data Access Object) for Genre entity.
 * Handles all database operations related to genres.
 */
public class GenreDao {

	/**
	 * Retrieves all genres from the database.
	 * 
	 * @return a list of all genres, or an empty list if none exist
	 */
	public List<Genre> listGenres() {
		List<Genre> genres = new ArrayList<>();
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM genre")) {
			
			while (resultSet.next()) {
				Genre genre = new Genre(
					resultSet.getInt("idgenre"),
					resultSet.getString("name")
				);
				genres.add(genre);
			}
			return genres;
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while fetching genres from database", e);
		}
	}

	/**
	 * Retrieves a specific genre by its name.
	 * 
	 * @param name the name of the genre to retrieve
	 * @return the Genre object if found, null otherwise
	 */
	public Genre getGenre(String name) {
		String sqlQuery = "SELECT * FROM genre WHERE name = ?";
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
			
			statement.setString(1, name);
			
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return new Genre(
						resultSet.getInt("idgenre"),
						resultSet.getString("name")
					);
				}
			}
			return null;
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while fetching genre by name: " + name, e);
		}
	}

	/**
	 * Adds a new genre to the database.
	 * 
	 * @param name the name of the genre to add
	 */
	public void addGenre(String name) {
		String sqlQuery = "INSERT INTO genre(name) VALUES(?)";
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
			
			statement.setString(1, name);
			statement.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while adding genre: " + name, e);
		}
	}
}