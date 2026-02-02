package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

/**
 * DAO (Data Access Object) for Movie entity.
 * Handles all database operations related to movies.
 */
public class MovieDao {

	/**
	 * Retrieves all movies from the database with their associated genre information.
	 * Uses a JOIN to combine movie and genre tables.
	 * 
	 * @return a list of all movies with their genres, or an empty list if none exist
	 */
	public List<Movie> listMovies() {
		List<Movie> movies = new ArrayList<>();
		String sqlQuery = "SELECT * FROM movie " +
						  "JOIN genre ON movie.genre_id = genre.idgenre";
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			
			while (resultSet.next()) {
				Movie movie = createMovieFromResultSet(resultSet);
				movies.add(movie);
			}
			return movies;
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while fetching movies from database", e);
		}
	}

	/**
	 * Retrieves all movies of a specific genre.
	 * Uses a JOIN with a WHERE clause to filter by genre name.
	 * 
	 * @param genreName the name of the genre to filter by
	 * @return a list of movies matching the genre, or an empty list if none exist
	 */
	public List<Movie> listMoviesByGenre(String genreName) {
		List<Movie> movies = new ArrayList<>();
		String sqlQuery = "SELECT * FROM movie " +
						  "JOIN genre ON movie.genre_id = genre.idgenre " +
						  "WHERE genre.name = ?";
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
			
			statement.setString(1, genreName);
			
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Movie movie = createMovieFromResultSet(resultSet);
					movies.add(movie);
				}
			}
			return movies;
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while fetching movies by genre: " + genreName, e);
		}
	}

	/**
	 * Adds a new movie to the database.
	 * The movie parameter should have all information except the id.
	 * Returns a new Movie object with the database-generated id.
	 * 
	 * @param movie the movie to add (without id)
	 * @return a new Movie object with the same information plus the generated id
	 */
	public Movie addMovie(Movie movie) {
		String sqlQuery = "INSERT INTO movie(title, release_date, genre_id, duration, director, summary) " +
						  "VALUES(?, ?, ?, ?, ?, ?)";
		
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
			
			statement.setString(1, movie.getTitle());
			statement.setDate(2, Date.valueOf(movie.getReleaseDate()));
			statement.setInt(3, movie.getGenre().getId());
			statement.setInt(4, movie.getDuration());
			statement.setString(5, movie.getDirector());
			statement.setString(6, movie.getSummary());
			
			statement.executeUpdate();
			
			// Retrieve the generated id
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int generatedId = generatedKeys.getInt(1);
					
					// Return a new Movie object with the generated id
					return new Movie(
						generatedId,
						movie.getTitle(),
						movie.getReleaseDate(),
						movie.getGenre(),
						movie.getDuration(),
						movie.getDirector(),
						movie.getSummary()
					);
				}
			}
			
			throw new RuntimeException("Failed to retrieve generated id for movie");
			
		} catch (SQLException e) {
			throw new RuntimeException("Error while adding movie: " + movie.getTitle(), e);
		}
	}
	
	/**
	 * Helper method to create a Movie object from a ResultSet.
	 * Expects the ResultSet to contain both movie and genre columns from a JOIN query.
	 * 
	 * @param resultSet the ResultSet positioned at a valid row
	 * @return a Movie object with all fields populated
	 * @throws SQLException if there's an error reading from the ResultSet
	 */
	private Movie createMovieFromResultSet(ResultSet resultSet) throws SQLException {
		// Create the Genre object from the joined genre table
		Genre genre = new Genre(
			resultSet.getInt("idgenre"),
			resultSet.getString("name")
		);
		
		// Create and return the Movie object
		return new Movie(
			resultSet.getInt("idmovie"),
			resultSet.getString("title"),
			resultSet.getDate("release_date").toLocalDate(),
			genre,
			resultSet.getInt("duration"),
			resultSet.getString("director"),
			resultSet.getString("summary")
		);
	}
}