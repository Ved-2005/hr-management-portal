package com.hrportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartmentDto(
    @NotBlank String name,
    String description,
    @NotNull Integer sickLeaves,
    @NotNull Integer casualLeaves,
    @NotNull Integer paidLeaves
) {}

