package com.challenge.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createEmployee(String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.uuid");
    }

    private static final String TERMINATE = "/api/v1/employee/{uuid}/terminate";

    private String validBody(String hireDate) {
        return """
                {"firstName":"Test","lastName":"User","salary":70000,"age":30,
                "jobTitle":"Designer","email":"test.user@example.com",
                "contractHireDate":"%s"}
                """
                .formatted(hireDate);
    }

    @Test
    void testGetAllEmployeesReturnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateEmployeeReturns201() throws Exception {
        String body =
                """
        {"firstName":"Jane","lastName":"Doe","salary":90000,"age":28,
         "jobTitle":"Analyst","email":"jane.doe@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.contractTerminationDate").isEmpty());
    }

    @Test
    void testGetEmployeeByUuidReturns200() throws Exception {
        String body =
                """
        {"firstName":"J","lastName":"J","salary":80000,"age":35,
         "jobTitle":"Developer","email":"J.J@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        String uuid = createEmployee(body);
        mockMvc.perform(get("/api/v1/employee/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.fullName").value("J J"));
    }

    @Test
    void testGetEmployeeByUuidReturns400ForInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/employee/{uuid}", "invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployeeByUuidReturns404ForNonExistentUuid() throws Exception {
        mockMvc.perform(get("/api/v1/employee/{uuid}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEmployeeWithBlankFirstNameReturns400() throws Exception {
        String body =
                """
        {"firstName":" ","lastName":"Doe","salary":75000,"age":32,
         "jobTitle":"Manager","email":"jane.doe@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("First name is required")));
    }

    @Test
    void testCreateEmployeeWithMissingFieldsReturns400() throws Exception {
        String body = """
        {"firstName":"A"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("lastName")));
    }

    @Test
    void testCreateEmployeeWithInvalidEmailReturns400() throws Exception {
        String body =
                """
        {"firstName":"B","lastName":"B","salary":75000,"age":32,
         "jobTitle":"Manager","email":"invalid-email",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email format")));
    }

    @Test
    void testCreateEmployeeWithNegativeSalaryReturns400() throws Exception {
        String body =
                """
        {"firstName":"C","lastName":"C","salary":-50000,"age":29,
         "jobTitle":"Analyst","email":"C.C@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Salary must be a positive value")));
    }

    @Test
    void testCreateEmployeeWithTerminationDateBeforeHireDateReturns400() throws Exception {
        String body =
                """
        {"firstName":"D","lastName":"D","salary":60000,"age":31,
         "jobTitle":"Consultant","email":"D.D@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z",
         "contractTerminationDate":"2025-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Contract termination date must be after contract hire date")));
    }

    @Test
    void testCreateEmployeeWithMalformedRequestReturns400() throws Exception {
        String body =
                """
        {"firstName":"E","lastName":"E","salary":abc,"age":30,
         "jobTitle":"Engineer","email":"E.E@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Malformed request body")));
    }

    @Test
    void testCreateEmployeeWithTerminationDateAfterHireDateReturns201() throws Exception {
        String body =
                """
        {"firstName":"F","lastName":"F","salary":70000,"age":30,
         "jobTitle":"Designer","email":"F.F@example.com",
         "contractHireDate":"2026-01-01T00:00:00Z",
         "contractTerminationDate":"2027-01-01T00:00:00Z"}
        """;
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.contractTerminationDate").value("2027-01-01T00:00:00Z"))
                .andExpect(status().isCreated());
    }

    @Test
    void testTerminateEmployeeReturns200() throws Exception {
        String uuid = createEmployee(validBody("2020-01-01T00:00:00Z"));
        mockMvc.perform(post(TERMINATE, uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractTerminationDate").isNotEmpty());
    }

    @Test
    void testTerminateTwiceKeepsOriginalDate() throws Exception {
        String uuid = createEmployee(validBody("2020-01-01T00:00:00Z"));
        MvcResult first = mockMvc.perform(post(TERMINATE, uuid))
                .andExpect(status().isOk())
                .andReturn();
        String firstDate = JsonPath.read(first.getResponse().getContentAsString(), "$.contractTerminationDate");
        mockMvc.perform(post(TERMINATE, uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractTerminationDate").value(firstDate));
    }

    @Test
    void testTerminateAlreadyTerminatedEmployeeKeepsExistingDate() throws Exception {
        String body =
                """
                {"firstName":"Test","lastName":"User","salary":70000,"age":30,
                "jobTitle":"Designer","email":"test.user@example.com",
                "contractHireDate":"2020-01-01T00:00:00Z",
                "contractTerminationDate":"2025-12-31T00:00:00Z"}
                """;
        String uuid = createEmployee(body);
        mockMvc.perform(post(TERMINATE, uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractTerminationDate").value("2025-12-31T00:00:00Z"));
    }

    @Test
    void testTerminateFutureHireReturns409() throws Exception {
        String futureHire = Instant.now().plus(30, ChronoUnit.DAYS).toString();
        String uuid = createEmployee(validBody(futureHire));
        mockMvc.perform(post(TERMINATE, uuid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("not started")));
    }

    @Test
    void testTerminateUnknownUUIDReturns404() throws Exception {
        mockMvc.perform(post(TERMINATE, "00000000-0000-0000-0000-000000000000")).andExpect(status().isNotFound());
    }

    @Test
    void testTerminateInvalidUUIDReturns400() throws Exception {
        mockMvc.perform(post(TERMINATE, "invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteEmployeeReturns405() throws Exception {
        String uuid = createEmployee(validBody("2020-01-01T00:00:00Z"));
        mockMvc.perform(delete("/api/v1/employee/{uuid}", uuid)).andExpect(status().isMethodNotAllowed());
    }
}
