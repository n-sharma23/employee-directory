package com.neeraj.employees.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = "DELETE FROM employees")
@Sql("classpath:data.sql")
class EmployeeControllerTest {

    private static final String NEW_EMPLOYEE = """
            {"firstName":"Nina","lastName":"Rao","email":"Nina.Rao@Example.com","department":"PRODUCT",
             "jobTitle":"Designer","salary":60000,"hireDate":"2025-02-01"}""";

    @Autowired
    MockMvc mvc;

    @Test
    void listsAndFiltersByDepartment() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)));
        mvc.perform(get("/api/employees").param("department", "ENGINEERING"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void createsEmployeeAndNormalisesEmail() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/employees/")))
                .andExpect(jsonPath("$.email").value("nina.rao@example.com"));
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidInputWithFieldMessages() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("email")))
                .andExpect(jsonPath("$.detail", containsString("firstName")));
    }

    @Test
    void unknownEmployeeIs404() throws Exception {
        mvc.perform(get("/api/employees/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/employees/999999")).andExpect(status().isNotFound());
    }

    @Test
    void raiseIsCappedAt50Percent() throws Exception {
        mvc.perform(post("/api/employees/1/raise").contentType(MediaType.APPLICATION_JSON).content("{\"percent\":500}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportsHeadcountPerDepartment() throws Exception {
        mvc.perform(get("/api/employees/stats/headcount"))
                .andExpect(jsonPath("$.ENGINEERING").value(2))
                .andExpect(jsonPath("$.SALES").value(1));
    }

    @Test
    void exportsAndImportsCsv() throws Exception {
        mvc.perform(get("/api/employees/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("employees.csv")))
                .andExpect(content().string(containsString("asha.verma@example.com")));

        var file = new MockMultipartFile("file", "new.csv", "text/csv", """
                first_name,last_name,email,department,job_title,salary,hire_date
                Omar,Ali,omar@example.com,FINANCE,Controller,81000,2023-03-01
                """.getBytes());
        mvc.perform(multipart("/api/employees/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(1));
        mvc.perform(get("/api/employees")).andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void importWithExistingEmailImportsNothing() throws Exception {
        var file = new MockMultipartFile("file", "dup.csv", "text/csv", """
                first_name,last_name,email,department,job_title,salary,hire_date
                Omar,Ali,omar@example.com,FINANCE,Controller,81000,2023-03-01
                Asha,Verma,asha.verma@example.com,ENGINEERING,Engineer,1,2023-03-01
                """.getBytes());
        mvc.perform(multipart("/api/employees/import").file(file)).andExpect(status().isConflict());
        mvc.perform(get("/api/employees")).andExpect(jsonPath("$", hasSize(5)));
    }
}
