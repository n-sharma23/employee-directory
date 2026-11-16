package com.neeraj.employees.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Employee extends Person {

    private Long id;
    private Department department;
    private String jobTitle;
    private BigDecimal salary;
    private LocalDate hireDate;

    public Employee(Long id, String firstName, String lastName, String email,
                    Department department, String jobTitle, BigDecimal salary, LocalDate hireDate) {
        super(firstName, lastName, email);
        this.id = id;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    @Override
    public String describeRole() {
        return jobTitle + " in " + department.getDisplayName();
    }

    /**
     * Raises salary by a percentage. Rejects negative values so a "raise"
     * can never silently become a pay cut.
     */
    public void applyRaise(BigDecimal percent) {
        if (percent == null || percent.signum() < 0) {
            throw new IllegalArgumentException("Raise percentage must be zero or positive");
        }
        BigDecimal factor = BigDecimal.ONE.add(percent.movePointLeft(2));
        salary = salary.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
