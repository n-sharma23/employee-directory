package com.neeraj.employees.model;

public enum Department {
    ENGINEERING("Engineering"),
    PRODUCT("Product"),
    SALES("Sales"),
    HR("Human Resources"),
    FINANCE("Finance");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
