package com.neeraj.employees.repository;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

    List<Employee> findAll();

    List<Employee> findByDepartment(Department department);

    Optional<Employee> findById(long id);

    boolean existsByEmail(String email);

    Employee save(Employee employee);

    /** Inserts all rows in one transaction: either every employee is saved or none is. */
    List<Employee> saveAll(List<Employee> employees);

    boolean update(Employee employee);

    boolean deleteById(long id);
}
