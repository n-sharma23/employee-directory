# Employee Directory

A small full-stack Java app for managing a company's employees. It is the capstone
project for the SkillUp Java track and pulls the courses together into one working
application: a Spring Boot REST API, a plain JDBC data layer on MySQL (or H2),
CSV import/export, and an HTML/CSS/JavaScript front end.

## Features

- List, add, edit and delete employees, and filter them by department
- Give an employee a percentage raise (capped at 50%)
- Headcount per department
- Export all employees to CSV, and import employees from CSV
- Validation on every input path (form, API and CSV), with clear error messages
- Light and dark mode

## Run it

You need JDK 25. Maven comes with the project through the wrapper (`mvnw`).

```bash
./mvnw spring-boot:run
```

Open <http://localhost:8080>. By default the app uses an in-memory H2 database in
MySQL compatibility mode and loads five sample employees, so nothing else needs
to be installed.

### Run against MySQL

```bash
export DB_USERNAME=root DB_PASSWORD=yourpassword   # DB_HOST, DB_PORT, DB_NAME are optional
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
```

The database `employee_directory` is created on first start, and `schema.sql` creates
the table. Credentials are read from environment variables and never committed.

### Run the tests

```bash
./mvnw test
```


## How the course topics map to the code

| Course | Where to look |
|---|---|
| Introduction to Software Engineering | Layered design (web → service → repository), tests, the PR-based review flow in [docs/PEER_REVIEW.md](docs/PEER_REVIEW.md) |
| Java Programming for Beginners | Collections and streams in `EmployeeService.headcount()`, `BigDecimal` money maths, `LocalDate`, records, text blocks |
| Object Oriented Programming in Java | Abstract class `Person` → `Employee` (inheritance, polymorphic `describeRole()`), encapsulation, `equals`/`hashCode`, the `Department` enum, custom exceptions, the `EmployeeRepository` interface with a JDBC implementation |
| Java App Development: Fundamentals, OOP & File I/O | `EmployeeCsv`: `BufferedReader`/`BufferedWriter`, a hand-written CSV parser with quoting, and `EmployeeCsvTest` round-tripping through a real file with `java.nio.file.Files` |
| Introduction to HTML, CSS & JavaScript | `src/main/resources/static`: semantic HTML, CSS custom properties with dark mode, responsive grid, `fetch` + `async/await`, DOM building with `textContent` |
| Getting Started with Git and GitHub | Feature branch → pull request → peer review → merge; PR template |
| Spring Framework for Java Development | Spring Boot, dependency injection through constructors, `@RestController`, `@RestControllerAdvice` with `ProblemDetail`, Bean Validation, profiles, externalised config |
| Java Development with Databases | `JdbcEmployeeRepository`: `DataSource` + HikariCP pool, `PreparedStatement`, generated keys, try-with-resources, a transaction with commit/rollback in `saveAll`; `schema.sql` DDL with primary key, unique and check constraints and an index |

## Design decisions

- **Plain JDBC instead of JPA or `JdbcTemplate`.** The databases course is about JDBC, so the
  connection, statement and result-set handling stays visible. In a production service,
  `JdbcClient` or Spring Data would remove most of this boilerplate.
- **Every query is a `PreparedStatement`.** User input is never concatenated into SQL,
  which rules out SQL injection.
- **CSV import is all-or-nothing.** One bad row (invalid data or a duplicate email)
  rejects the whole file and reports the line number, so a half-imported file never
  leaves the directory in an unclear state. The database transaction in `saveAll`
  backs this up.
- **H2 in MySQL mode by default.** Reviewers can clone and run the project without
  installing a database. The same `schema.sql` works on MySQL 8.
- **No authentication.** This is a local learning project. Anything deployed would need
  login and server-side authorization before exposing salary data.

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/employees?department=ENGINEERING` | List employees, optionally filtered |
| GET | `/api/employees/{id}` | Get one employee |
| POST | `/api/employees` | Create an employee |
| PUT | `/api/employees/{id}` | Update an employee |
| DELETE | `/api/employees/{id}` | Delete an employee |
| POST | `/api/employees/{id}/raise` | Body `{"percent": 5}` |
| GET | `/api/employees/stats/headcount` | Count per department |
| GET | `/api/employees/export` | Download CSV |
| POST | `/api/employees/import` | Upload CSV (multipart field `file`) |

Errors come back as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem JSON,
with a readable `detail` message.

## Project layout

```
src/main/java/com/neeraj/employees
├── model/        Person, Employee, Department, EmployeeNotFoundException
├── repository/   EmployeeRepository, JdbcEmployeeRepository
├── service/      EmployeeService, EmployeeCsv, EmployeeRequest
└── web/          EmployeeController, ApiExceptionHandler
src/main/resources
├── schema.sql, data.sql
├── application.properties, application-mysql.properties
└── static/       index.html, styles.css, app.js
```
