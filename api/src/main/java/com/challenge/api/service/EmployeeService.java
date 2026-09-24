package com.challenge.api.service;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final Map<UUID, Employee> employeeMap = new ConcurrentHashMap<>();

    public EmployeeService() {
        seedData();
    }

    private void seedData() {
        UUID seedUUID = UUID.randomUUID();
        Employee seedEmployee = new EmployeeProfile(
                seedUUID,
                "John",
                "Doe",
                50000,
                30,
                "Software Engineer",
                "johndoe@example.com",
                java.time.Instant.parse("2020-01-01T00:00:00Z"),
                null);
        employeeMap.put(seedUUID, seedEmployee);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }

    public Employee getEmployeeByID(UUID id) {
        Employee employee = employeeMap.get(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee with ID " + id + " not found");
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
}
