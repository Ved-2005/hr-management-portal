package com.hrportal.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentDto(@NotBlank String name, String description) {}

