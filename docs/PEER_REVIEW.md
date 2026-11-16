# Peer review guide

Thanks for reviewing. The code arrives as a pull request so comments can sit
next to the lines they're about.

## How to review

1. Open the pull request, then read the **Files changed** tab.
2. Check out the branch and run it:
   ```bash
   git clone git@github.com:n-sharma23/employee-directory.git
   cd employee-directory
   git checkout feature/employee-directory
   ./mvnw test
   ./mvnw spring-boot:run   # then open http://localhost:8080
   ```
3. Leave inline comments, then submit a review: **Approve** or **Request changes**.

## Checklist

### Java and OOP
- [ ] Classes have one clear job, and names say what they do
- [ ] Inheritance (`Person` → `Employee`) and the interface (`EmployeeRepository`) are used sensibly
- [ ] Fields are private, with no logic leaking out of the model
- [ ] Exceptions are specific and handled at the right level

### Database and JDBC
- [ ] Every query uses `PreparedStatement` with no string-built SQL
- [ ] Connections, statements and result sets are always closed (try-with-resources)
- [ ] `saveAll` really rolls back on failure (see `JdbcEmployeeRepositoryTest`)
- [ ] Schema constraints (primary key, unique email, check salary) match the Java validation

### File I/O
- [ ] CSV export and import round-trip without data loss, including commas and quotes in text
- [ ] Bad files give a clear error with a line number

### Spring
- [ ] Dependencies are injected through constructors
- [ ] Validation errors return 400, missing records 404, duplicates 409
- [ ] No secrets in the repository (MySQL credentials come from environment variables)

### Front end
- [ ] Works in light and dark mode and at phone width
- [ ] Forms show clear errors
- [ ] User data is never inserted with `innerHTML`

### Quality
- [ ] `./mvnw test` passes locally
- [ ] Tests cover the main paths and the important edge cases
- [ ] The README explains how to run the project
