package com.neeraj.employees.repository;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Plain JDBC on purpose, rather than JdbcTemplate or JPA, so the connection,
 * statement and result-set handling from the course stays visible. Connections
 * come from Spring Boot's HikariCP pool; closing one returns it to the pool.
 * Every query is a PreparedStatement, so user input is never concatenated into SQL.
 */
@Repository
public class JdbcEmployeeRepository implements EmployeeRepository {

    private static final String COLUMNS =
            "id, first_name, last_name, email, department, job_title, salary, hire_date";

    private final DataSource dataSource;

    public JdbcEmployeeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM employees ORDER BY last_name, first_name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapAll(rs);
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not load employees", e);
        }
    }

    @Override
    public List<Employee> findByDepartment(Department department) {
        String sql = "SELECT " + COLUMNS + " FROM employees WHERE department = ? ORDER BY last_name, first_name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not load employees for " + department, e);
        }
    }

    @Override
    public Optional<Employee> findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM employees WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not load employee " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM employees WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not check email", e);
        }
    }

    @Override
    public Employee save(Employee employee) {
        try (Connection conn = dataSource.getConnection()) {
            insert(conn, employee);
            return employee;
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not save employee", e);
        }
    }

    @Override
    public List<Employee> saveAll(List<Employee> employees) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Employee employee : employees) {
                    insert(conn, employee);
                }
                conn.commit();
                return employees;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not save employees; nothing was imported", e);
        }
    }

    @Override
    public boolean update(Employee employee) {
        String sql = """
                UPDATE employees
                   SET first_name = ?, last_name = ?, email = ?, department = ?,
                       job_title = ?, salary = ?, hire_date = ?
                 WHERE id = ?""";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindFields(ps, employee);
            ps.setLong(8, employee.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not update employee " + employee.getId(), e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessFailure("Could not delete employee " + id, e);
        }
    }

    private void insert(Connection conn, Employee employee) throws SQLException {
        String sql = """
                INSERT INTO employees (first_name, last_name, email, department, job_title, salary, hire_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindFields(ps, employee);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setId(keys.getLong(1));
                }
            }
        }
    }

    private static void bindFields(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getFirstName());
        ps.setString(2, e.getLastName());
        ps.setString(3, e.getEmail());
        ps.setString(4, e.getDepartment().name());
        ps.setString(5, e.getJobTitle());
        ps.setBigDecimal(6, e.getSalary());
        ps.setDate(7, Date.valueOf(e.getHireDate()));
    }

    private static List<Employee> mapAll(ResultSet rs) throws SQLException {
        List<Employee> result = new ArrayList<>();
        while (rs.next()) {
            result.add(map(rs));
        }
        return result;
    }

    private static Employee map(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                Department.valueOf(rs.getString("department")),
                rs.getString("job_title"),
                rs.getBigDecimal("salary"),
                rs.getDate("hire_date").toLocalDate());
    }
}
