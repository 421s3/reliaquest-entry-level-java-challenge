package com.challenge.api.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public class CreateEmployeeRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Salary is required") @Positive(message = "Salary must be a positive value") private Integer salary;

    @NotNull(message = "Age is required") @Positive(message = "Age must be a positive value") private Integer age;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Contract hire date is required") private Instant contractHireDate;

    private Instant contractTerminationDate;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getContractHireDate() {
        return contractHireDate;
    }

    public void setContractHireDate(Instant contractHireDate) {
        this.contractHireDate = contractHireDate;
    }

    public Instant getContractTerminationDate() {
        return contractTerminationDate;
    }

    public void setContractTerminationDate(Instant contractTerminationDate) {
        this.contractTerminationDate = contractTerminationDate;
    }

    @AssertTrue(message = "Contract termination date must be after contract hire date")
    public boolean isHireBeforeTermination() {
        if (contractHireDate != null && contractTerminationDate != null) {
            return contractTerminationDate.isAfter(contractHireDate);
        }
        return true;
    }
}
