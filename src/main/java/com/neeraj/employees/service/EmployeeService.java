package com.neeraj.employees.service;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import com.neeraj.employees.model.EmployeeNotFoundException;
import com.neeraj.employees.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> list(Department department) {
        return department == null ? repository.findAll() : repository.findByDepartment(department);
    }

    public Employee get(long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Employee create(EmployeeRequest request) {
        Employee employee = request.toEmployee(null);
        if (repository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException(employee.getEmail());
        }
        return repository.save(employee);
    }

    public Employee update(long id, EmployeeRequest request) {
        Employee existing = get(id);
        Employee updated = request.toEmployee(id);
        boolean emailChanged = !existing.getEmail().equals(updated.getEmail());
        if (emailChanged && repository.existsByEmail(updated.getEmail())) {
            throw new DuplicateEmailException(updated.getEmail());
        }
        repository.update(updated);
        return updated;
    }

    public Employee giveRaise(long id, BigDecimal percent) {
        Employee employee = get(id);
        employee.applyRaise(percent);
        repository.update(employee);
        return employee;
    }

    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new EmployeeNotFoundException(id);
        }
    }

    /** Headcount per department, in the order departments are declared in the enum. */
    public Map<Department, Long> headcount() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.counting()));
    }

    /** Imports all rows or none; duplicate emails (in the file or the database) reject the whole import. */
    public List<Employee> importAll(List<Employee> employees) {
        Set<String> seen = new HashSet<>();
        for (Employee e : employees) {
            if (!seen.add(e.getEmail()) || repository.existsByEmail(e.getEmail())) {
                throw new DuplicateEmailException(e.getEmail());
            }
        }
        return repository.saveAll(employees);
    }
}
