# Movies Collection – JDBC Practical Work (PW05)

## Author
Naomi Nketsiah  
Master 1 Software Engineering – JUNIA ISEN  
Java 2 – JDBC Practical Work (PW05)

## Project Description
This project implements database access in Java using JDBC and SQLite,
following the DAO (Data Access Object) design pattern.

It includes management of movies and genres, with unit tests validating
all required operations.

## Bonus Stages Implemented
- **Bonus Stage 1**: Database access refactored to use `DriverManager`
  instead of `SQLiteDataSource`, making the code database-agnostic.
- **Bonus Stage 2**: `Optional<Genre>` used instead of `null` in
  `getGenre` to improve null safety.

## Technologies
- Java 21
- JDBC
- SQLite
- Maven
- JUnit 5
- AssertJ

## How to Run Tests
```bash
mvn test
