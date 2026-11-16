package com.neeraj.employees.model;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(long id) {
        super("Employee " + id + " not found");
    }
}
