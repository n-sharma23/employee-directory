package com.neeraj.employees.service;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads and writes employees as CSV. Works on Reader/Writer rather than files
 * so the same code serves HTTP uploads, downloads and files on disk
 * (wrap a Files.newBufferedReader / newBufferedWriter).
 * Hand-written instead of a CSV library to keep the file I/O visible; it
 * supports quoted fields, which job titles like "Engineer, Backend" need.
 */
@Component
public class EmployeeCsv {

    static final String HEADER = "first_name,last_name,email,department,job_title,salary,hire_date";
    private static final int COLUMN_COUNT = 7;

    private final Validator validator;

    public EmployeeCsv(Validator validator) {
        this.validator = validator;
    }

    public void write(List<Employee> employees, Writer out) throws IOException {
        BufferedWriter writer = out instanceof BufferedWriter bw ? bw : new BufferedWriter(out);
        writer.write(HEADER);
        writer.newLine();
        for (Employee e : employees) {
            writer.write(String.join(",",
                    quote(e.getFirstName()), quote(e.getLastName()), quote(e.getEmail()),
                    e.getDepartment().name(), quote(e.getJobTitle()),
                    e.getSalary().toPlainString(), e.getHireDate().toString()));
            writer.newLine();
        }
        writer.flush();
    }

    public List<Employee> read(Reader in) throws IOException {
        List<Employee> result = new ArrayList<>();
        BufferedReader reader = in instanceof BufferedReader br ? br : new BufferedReader(in);
        String header = reader.readLine();
        if (header == null || !header.strip().equalsIgnoreCase(HEADER)) {
            throw new CsvFormatException(1, "expected header: " + HEADER);
        }
        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            result.add(parseRow(line, lineNumber));
        }
        return result;
    }

    private Employee parseRow(String line, int lineNumber) {
        List<String> f = split(line, lineNumber);
        if (f.size() != COLUMN_COUNT) {
            throw new CsvFormatException(lineNumber, "expected " + COLUMN_COUNT + " columns, found " + f.size());
        }
        EmployeeRequest request;
        try {
            request = new EmployeeRequest(f.get(0), f.get(1), f.get(2),
                    Department.valueOf(f.get(3).strip().toUpperCase()), f.get(4),
                    new BigDecimal(f.get(5).strip()), LocalDate.parse(f.get(6).strip()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new CsvFormatException(lineNumber, e.getMessage());
        }
        // Same rules as the REST API, so a file can't sneak in data the form would reject.
        Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new CsvFormatException(lineNumber, violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
        return request.toEmployee(null);
    }

    private static List<String> split(String line, int lineNumber) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new CsvFormatException(lineNumber, "unclosed quote");
        }
        fields.add(current.toString());
        return fields;
    }

    private static String quote(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static class CsvFormatException extends RuntimeException {
        public CsvFormatException(int line, String detail) {
            super("CSV line " + line + ": " + detail);
        }
    }
}
