# PW05 – JDBC (Java 2)

## Description
This project is part of the **Java 2 – Practical Work 05 (JDBC)**.
It demonstrates how to use **JDBC** and the **DAO pattern** to interact with a
**SQLite database**.

The project includes DAO implementations for `Genre` and `Movie`, with unit
tests validating all database operations.

## Technologies
- Java 21
- Maven
- JDBC
- SQLite
- JUnit 5

## Project Structure
- `DataSourceFactory`: provides database connections
- `GenreDao`: JDBC operations for genres
- `MovieDao`: JDBC operations for movies (JOINs, generated IDs)
- Unit tests located in `src/test/java`

## How to run the tests
```bash
mvn test

```md
## Author
Naomi Nketsiah