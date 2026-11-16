package com.neeraj.employees.service;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeCsvTest {

    private final EmployeeCsv csv = new EmployeeCsv(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void roundTripsThroughAFileOnDisk(@TempDir Path dir) throws Exception {
        Employee original = new Employee(null, "Lena", "Berg", "lena@example.com", Department.SALES,
                "Account Executive, \"Nordics\"", new BigDecimal("70000.50"), LocalDate.of(2020, 11, 23));
        Path file = dir.resolve("employees.csv");

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            csv.write(List.of(original), writer);
        }
        List<Employee> read;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            read = csv.read(reader);
        }

        assertThat(read).singleElement().satisfies(e -> {
            assertThat(e.getJobTitle()).isEqualTo("Account Executive, \"Nordics\"");
            assertThat(e.getSalary()).isEqualByComparingTo("70000.50");
            assertThat(e.getHireDate()).isEqualTo(LocalDate.of(2020, 11, 23));
            assertThat(e.getDepartment()).isEqualTo(Department.SALES);
        });
    }

    @Test
    void rejectsWrongHeader() {
        assertThatThrownBy(() -> csv.read(new StringReader("name,email\n")))
                .hasMessageContaining("line 1");
    }

    @Test
    void reportsLineNumberOfBadRow() {
        String input = EmployeeCsv.HEADER + "\n"
                + "A,B,a@example.com,SALES,Rep,100,2024-01-01\n"
                + "C,D,c@example.com,NOT_A_DEPT,Rep,100,2024-01-01\n";
        assertThatThrownBy(() -> csv.read(new StringReader(input)))
                .hasMessageStartingWith("CSV line 3");
    }

    @Test
    void appliesSameValidationAsTheApi() {
        String input = EmployeeCsv.HEADER + "\n"
                + " ,B,not-an-email,SALES,Rep,-5,2024-01-01\n";
        assertThatThrownBy(() -> csv.read(new StringReader(input)))
                .hasMessageContaining("email")
                .hasMessageContaining("firstName")
                .hasMessageContaining("salary");
    }
}
