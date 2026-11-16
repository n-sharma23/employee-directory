package com.neeraj.employees.model;

import java.util.Objects;

/**
 * Base type for anyone the directory knows about. Abstract because a bare
 * "person" is never stored; subclasses decide what extra data they carry.
 */
public abstract class Person {

    private String firstName;
    private String lastName;
    private String email;

    protected Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Each subclass describes its role in the organisation (polymorphism). */
    public abstract String describeRole();

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person other)) return false;
        return Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
