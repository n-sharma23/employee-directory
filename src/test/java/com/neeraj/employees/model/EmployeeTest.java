package com.neeraj.employees.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    private Employee employee(String salary) {
        return new Employee(1L, "Asha", "Verma", "asha@example.com",
                Department.ENGINEERING, "Backend Engineer", new BigDecimal(salary), LocalDate.of(2022, 4, 11));
    }

    @Test
    void raiseIncreasesSalaryByPercentageRoundedToCents() {
        Employee e = employee("85000.00");
        e.applyRaise(new BigDecimal("3.5"));
        assertThat(e.getSalary()).isEqualByComparingTo("87975.00");
    }

    @Test
    void negativeRaiseIsRejected() {
        Employee e = employee("85000.00");
        assertThatThrownBy(() -> e.applyRaise(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(e.getSalary()).isEqualByComparingTo("85000.00");
    }

    @Test
    void describeRoleUsesDepartmentDisplayName() {
        Person p = employee("1"); // used through the abstract type
        assertThat(p.describeRole()).isEqualTo("Backend Engineer in Engineering");
        assertThat(p.getFullName()).isEqualTo("Asha Verma");
    }

    @Test
    void peopleWithTheSameEmailAreEqual() {
        assertThat(employee("1")).isEqualTo(employee("2")).hasSameHashCodeAs(employee("3"));
    }
}
