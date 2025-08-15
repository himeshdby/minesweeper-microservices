# Minesweeper Microservices Project (Java 17)

## Modules

- **minesweeper-game**: Core minesweeper logic and model.
- **minesweeper-cli**: CLI simulation (user-facing).
- **Tests**: Demonstrate success and failure gameplay.

## Requirements

- Java 17+
- Maven 3.8+

## Build

```sh
mvn clean install
```

## Run (CLI)

```sh
cd minesweeper-cli
mvn exec:java -Dexec.mainClass="com.minesweeper.cli.MinesweeperCLI"
```

## Tests

```sh
mvn test
```

## Design & Assumptions

- Microservices simulated via Maven modules (for demonstration).
- CLI accepts scripted moves for automatic testing.
- Mines placed randomly, but can be seeded for repeatable tests.
- Follows clean code, OOP, and SOLID principles.