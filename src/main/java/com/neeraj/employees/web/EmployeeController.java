package com.neeraj.employees.web;

import com.neeraj.employees.model.Department;
import com.neeraj.employees.model.Employee;
import com.neeraj.employees.service.EmployeeCsv;
import com.neeraj.employees.service.EmployeeRequest;
import com.neeraj.employees.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final EmployeeCsv csv;

    public EmployeeController(EmployeeService service, EmployeeCsv csv) {
        this.service = service;
        this.csv = csv;
    }

    @GetMapping
    public List<Employee> list(@RequestParam(required = false) Department department) {
        return service.list(department);
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        Employee created = service.create(request);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/raise")
    public Employee raise(@PathVariable long id, @Valid @RequestBody RaiseRequest request) {
        return service.giveRaise(id, request.percent());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @GetMapping("/stats/headcount")
    public Map<Department, Long> headcount() {
        return service.headcount();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"");
        csv.write(service.list(null), response.getWriter());
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public Map<String, Integer> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            List<Employee> imported = service.importAll(csv.read(reader));
            return Map.of("imported", imported.size());
        }
    }

    /** Capped at 50% so a typo (500 instead of 5) can't multiply someone's salary. */
    public record RaiseRequest(@NotNull @DecimalMin("0") @DecimalMax("50") BigDecimal percent) {
    }
}
