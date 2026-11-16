package com.neeraj.employees.repository;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(statements = "DELETE FROM employees")
@Sql("classpath:data.sql")
class JdbcEmployeeRepositoryTest {

    @Autowired
    EmployeeRepository repository;

    private static Employee newEmployee(String email) {
        return new Employee(null, "Test", "User", email, Department.FINANCE,
                "Analyst", new BigDecimal("50000.00"), LocalDate.of(2024, 1, 2));
    }

    @Test
    void findsSeedData() {
        assertThat(repository.findAll()).hasSize(5);
        assertThat(repository.findByDepartment(Department.ENGINEERING))
                .extracting(Employee::getLastName)
                .containsExactly("Khan", "Verma");
    }

    @Test
    void createReadUpdateDelete() {
        Employee saved = repository.save(newEmployee("test@example.com"));
        assertThat(saved.getId()).isNotNull();

        saved.setJobTitle("Senior Analyst");
        assertThat(repository.update(saved)).isTrue();
        assertThat(repository.findById(saved.getId()))
                .hasValueSatisfying(e -> assertThat(e.getJobTitle()).isEqualTo("Senior Analyst"));

        assertThat(repository.deleteById(saved.getId())).isTrue();
        assertThat(repository.findById(saved.getId())).isEmpty();
        assertThat(repository.deleteById(saved.getId())).isFalse();
    }

    @Test
    void saveAllRollsBackEverythingWhenOneRowFails() {
        // Second row breaks the unique email constraint, so the first must not survive either.
        List<Employee> batch = List.of(newEmployee("new@example.com"), newEmployee("asha.verma@example.com"));

        assertThatThrownBy(() -> repository.saveAll(batch)).isInstanceOf(DataAccessFailure.class);

        assertThat(repository.existsByEmail("new@example.com")).isFalse();
        assertThat(repository.findAll()).hasSize(5);
    }
}
