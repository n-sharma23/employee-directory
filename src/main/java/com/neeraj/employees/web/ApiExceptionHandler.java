package com.neeraj.employees.web;

import com.neeraj.employees.model.EmployeeNotFoundException;
import com.neeraj.employees.repository.DataAccessFailure;
import com.neeraj.employees.service.DuplicateEmailException;
import com.neeraj.employees.service.EmployeeCsv.CsvFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Turns domain exceptions into RFC 9457 problem responses with clean messages. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail notFound(EmployeeNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail duplicate(DuplicateEmailException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({CsvFormatException.class, IllegalArgumentException.class})
    public ProblemDetail badInput(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    /** SQL details stay in the server log; the client only learns that storage failed. */
    @ExceptionHandler(DataAccessFailure.class)
    public ProblemDetail storage(DataAccessFailure e) {
        log.error(e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database error, please try again");
    }
}
