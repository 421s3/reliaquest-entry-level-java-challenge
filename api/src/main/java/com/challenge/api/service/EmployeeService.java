package com.challenge.api.service;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.exception.InvalidEmployeeStateException;
import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeProfile;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final Map<UUID, Employee> employeeMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    public EmployeeService() {
        seedData();
    }

    private void seedData() {
        store(new EmployeeProfile(
                UUID.randomUUID(),
                "John",
                "Doe",
                50000,
                30,
                "Software Engineer",
                "john.doe@example.com",
                Instant.parse("2020-01-01T00:00:00Z"),
                null));

        store(new EmployeeProfile(
                UUID.randomUUID(),
                "Jane",
                "Doe",
                120000,
                45,
                "Engineering Manager",
                "jane.doe@example.com",
                Instant.parse("2018-01-15T00:00:00Z"),
                null));

        store(new EmployeeProfile(
                UUID.randomUUID(),
                "Alex",
                "Alex",
                62000,
                27,
                "QA Analyst",
                "alex.alex@example.com",
                Instant.parse("2022-09-12T00:00:00Z"),
                Instant.parse("2025-11-30T00:00:00Z")));

        store(new EmployeeProfile(
                UUID.randomUUID(),
                "X",
                "X",
                95000,
                29,
                "Data Engineer",
                "x.x@example.com",
                Instant.now().plus(90, ChronoUnit.DAYS),
                null));
    }

    private void store(Employee employee) {
        employeeMap.put(employee.getUuid(), employee);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }

    public Employee getEmployeeByUuid(UUID id) {
        Employee employee = employeeMap.get(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee with UUID " + id + " not found");
        }
        return employee;
    }

    public Employee createEmployee(CreateEmployeeRequest request) {
        EmployeeProfile employee = new EmployeeProfile(
                UUID.randomUUID(),
                request.getFirstName(),
                request.getLastName(),
                request.getSalary(),
                request.getAge(),
                request.getJobTitle(),
                request.getEmail(),
                request.getContractHireDate(),
                request.getContractTerminationDate());
        employeeMap.put(employee.getUuid(), employee);
        return employee;
    }

    public Employee terminateEmployee(UUID uuid) {
        Employee employee = getEmployeeByUuid(uuid);
        Instant now = Instant.now();
        if (employee.getContractTerminationDate() != null) {
            return employee;
        } else if (employee.getContractHireDate().isAfter(now)) {
            throw new InvalidEmployeeStateException("Employee has not started yet. Cannot terminate before hire date.");
        }

        employee.setContractTerminationDate(now);
        log.info("Terminated employee {}", uuid);
        return employee;
    }
}
