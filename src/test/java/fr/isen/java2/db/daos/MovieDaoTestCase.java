package fr.isen.java2.db.daos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

/**
 * Test cases for MovieDao class.
 * Tests all CRUD operations for movies.
 */
public class MovieDaoTestCase {
	
	private final MovieDao movieDao = new MovieDao();
	
	@BeforeEach
	public void initDb() throws Exception {
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement stmt = connection.createStatement()) {
			
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS movie (\r\n"
					+ "  idmovie INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n" + "  title VARCHAR(100) NOT NULL,\r\n"
					+ "  release_date DATETIME NULL,\r\n" + "  genre_id INT NOT NULL,\r\n" + "  duration INT NULL,\r\n"
					+ "  director VARCHAR(100) NOT NULL,\r\n" + "  summary MEDIUMTEXT NULL,\r\n"
					+ "  CONSTRAINT genre_fk FOREIGN KEY (genre_id) REFERENCES genre (idgenre));");
			stmt.executeUpdate("DELETE FROM movie");
			stmt.executeUpdate("DELETE FROM genre");
			stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='movie'");
			stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");
			stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
			stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
			stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
					+ "VALUES (1, 'Title 1', '2015-11-26 12:00:00.000', 1, 120, 'director 1', 'summary of the first movie')");
			stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
					+ "VALUES (2, 'My Title 2', '2015-11-14 12:00:00.000', 2, 114, 'director 2', 'summary of the second movie')");
			stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
					+ "VALUES (3, 'Third title', '2015-12-12 12:00:00.000', 2, 176, 'director 3', 'summary of the third movie')");
		}
		// Connection and Statement automatically closed here!
	}
	
	/**
	 * Test that listMovies() retrieves all movies with their genre information.
	 */
	@Test
	public void shouldListMovies() {
		// WHEN
		List<Movie> movies = movieDao.listMovies();
		
		// THEN
		assertThat(movies).hasSize(3);
		assertThat(movies).extracting("id", "title", "director")
			.containsOnly(
				tuple(1, "Title 1", "director 1"),
				tuple(2, "My Title 2", "director 2"),
				tuple(3, "Third title", "director 3")
			);
		
		// Verify that genres are properly loaded
		assertThat(movies).extracting("genre.id", "genre.name")
			.containsOnly(
				tuple(1, "Drama"),
				tuple(2, "Comedy"),
				tuple(2, "Comedy")
			);
		
		// Verify specific movie details
		Movie firstMovie = movies.stream()
			.filter(m -> m.getId() == 1)
			.findFirst()
			.orElse(null);
		assertThat(firstMovie).isNotNull();
		assertThat(firstMovie.getTitle()).isEqualTo("Title 1");
		assertThat(firstMovie.getDuration()).isEqualTo(120);
		assertThat(firstMovie.getSummary()).isEqualTo("summary of the first movie");
		assertThat(firstMovie.getGenre().getName()).isEqualTo("Drama");
	}
	
	/**
	 * Test that listMoviesByGenre() filters movies correctly by genre name.
	 */
	@Test
	public void shouldListMoviesByGenre() {
		// WHEN - Get only Comedy movies
		List<Movie> comedyMovies = movieDao.listMoviesByGenre("Comedy");
		
		// THEN
		assertThat(comedyMovies).hasSize(2);
		assertThat(comedyMovies).extracting("id", "title")
			.containsOnly(
				tuple(2, "My Title 2"),
				tuple(3, "Third title")
			);
		
		// Verify all returned movies have Comedy genre
		assertThat(comedyMovies).allMatch(m -> m.getGenre().getName().equals("Comedy"));
		
		// WHEN - Get only Drama movies
		List<Movie> dramaMovies = movieDao.listMoviesByGenre("Drama");
		
		// THEN
		assertThat(dramaMovies).hasSize(1);
		assertThat(dramaMovies.get(0).getTitle()).isEqualTo("Title 1");
		assertThat(dramaMovies.get(0).getGenre().getName()).isEqualTo("Drama");
		
		// WHEN - Get movies for non-existent genre
		List<Movie> unknownMovies = movieDao.listMoviesByGenre("Western");
		
		// THEN
		assertThat(unknownMovies).isEmpty();
	}
	
	/**
	 * Test that addMovie() correctly inserts a new movie and returns it with generated id.
	 */
	@Test
	public void shouldAddMovie() throws Exception {
		// GIVEN - Create a new movie to add
		Genre dramaGenre = new Genre(1, "Drama");
		LocalDate releaseDate = LocalDate.of(2020, 5, 15);
		Movie newMovie = new Movie(
			"Inception",
			releaseDate,
			dramaGenre,
			148,
			"Christopher Nolan",
			"A thief who steals corporate secrets through dream-sharing technology"
		);
		
		// Verify the movie doesn't have an id yet
		assertThat(newMovie.getId()).isNull();
		
		// WHEN - Add the movie
		Movie addedMovie = movieDao.addMovie(newMovie);
		
		// THEN - Verify the returned movie has an id
		assertThat(addedMovie).isNotNull();
		assertThat(addedMovie.getId()).isNotNull();
		assertThat(addedMovie.getId()).isGreaterThan(0);
		
		// Verify all fields are preserved
		assertThat(addedMovie.getTitle()).isEqualTo("Inception");
		assertThat(addedMovie.getReleaseDate()).isEqualTo(releaseDate);
		assertThat(addedMovie.getGenre().getId()).isEqualTo(1);
		assertThat(addedMovie.getDuration()).isEqualTo(148);
		assertThat(addedMovie.getDirector()).isEqualTo("Christopher Nolan");
		assertThat(addedMovie.getSummary()).isEqualTo("A thief who steals corporate secrets through dream-sharing technology");
		
		// Verify the movie was actually inserted in the database
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM movie WHERE title='Inception'")) {
			
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt("idmovie")).isEqualTo(addedMovie.getId());
			assertThat(resultSet.getString("title")).isEqualTo("Inception");
			assertThat(resultSet.getInt("genre_id")).isEqualTo(1);
			assertThat(resultSet.getInt("duration")).isEqualTo(148);
			assertThat(resultSet.getString("director")).isEqualTo("Christopher Nolan");
			assertThat(resultSet.next()).isFalse();
		}
		// Resources automatically closed here!
		
		// Verify we can retrieve it through listMovies
		List<Movie> allMovies = movieDao.listMovies();
		assertThat(allMovies).hasSize(4); // 3 initial + 1 new
		assertThat(allMovies).anyMatch(m -> m.getTitle().equals("Inception"));
	}
}