package com.hrportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeDto(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String username,
    String designation,
    @Positive Double salary,
    @NotNull Long departmentId
) {}
