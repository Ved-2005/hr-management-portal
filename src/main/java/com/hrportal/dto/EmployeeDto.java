package com.hrportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

public record EmployeeDto(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email @NotBlank String email,
    String designation,
    @Positive Double salary,
    @NotNull Long departmentId
) {}
