package com.neeraj.employees.service;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** What a client may send to create or update an employee. The id is never accepted from the client. */
public record EmployeeRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Email @Size(max = 100) String email,
        @NotNull Department department,
        @NotBlank @Size(max = 100) String jobTitle,
        @NotNull @PositiveOrZero BigDecimal salary,
        @NotNull @PastOrPresent LocalDate hireDate) {

    public Employee toEmployee(Long id) {
        return new Employee(id, firstName.strip(), lastName.strip(), email.strip().toLowerCase(),
                department, jobTitle.strip(), salary, hireDate);
    }
}
