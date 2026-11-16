package com.neeraj.employees.repository;

/** Wraps checked SQLExceptions so callers above the repository don't depend on JDBC. */
public class DataAccessFailure extends RuntimeException {

    public DataAccessFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
