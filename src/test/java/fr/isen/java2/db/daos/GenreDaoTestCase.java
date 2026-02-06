package fr.isen.java2.db.daos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.isen.java2.db.entities.Genre;

/**
 * Test cases for GenreDao.
 * 
 * BONUS STAGE 2: Updated to work with Optional&lt;Genre&gt; instead of null.
 */
public class GenreDaoTestCase {

	private final GenreDao genreDao = new GenreDao();

	@BeforeEach
	public void initDatabase() throws Exception {
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement stmt = connection.createStatement()) {
			
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
			stmt.executeUpdate("DELETE FROM genre");
			stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");
			stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
			stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
			stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (3,'Thriller')");
		}
		// Connection and Statement automatically closed here!
	}

	@Test
	public void shouldListGenres() {
		// WHEN
		List<Genre> genres = genreDao.listGenres();
		// THEN
		assertThat(genres).hasSize(3);
		assertThat(genres).extracting("id", "name").containsOnly(tuple(1, "Drama"), tuple(2, "Comedy"),
				tuple(3, "Thriller"));
	}
	
	@Test
	public void shouldGetGenreByName() {
		// WHEN
		Optional<Genre> genreOptional = genreDao.getGenre("Comedy");
		
		// THEN - BONUS STAGE 2: Check Optional is present
		assertThat(genreOptional).isPresent();
		
		// Extract the Genre from Optional
		Genre genre = genreOptional.get();
		assertThat(genre.getId()).isEqualTo(2);
		assertThat(genre.getName()).isEqualTo("Comedy");
	}
	
	@Test
	public void shouldNotGetUnknownGenre() {
		// WHEN
		Optional<Genre> genreOptional = genreDao.getGenre("Unknown");
		
		// THEN - BONUS STAGE 2: Expect Optional.empty() instead of null
		// This is meaningful: empty Optional clearly indicates "not found"
		// No risk of NullPointerException!
		assertThat(genreOptional).isEmpty();
		assertThat(genreOptional).isNotPresent();
	}
	
	@Test
	public void shouldAddGenre() throws Exception {
		// WHEN 
		genreDao.addGenre("Western");
		
		// THEN - Verify in database using try-with-resources
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM genre WHERE name='Western'")) {
			
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt("idgenre")).isNotNull();
			assertThat(resultSet.getString("name")).isEqualTo("Western");
			assertThat(resultSet.next()).isFalse();
		}
		// Resources automatically closed here, even if assertions fail!
	}
}