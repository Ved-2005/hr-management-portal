package com.hrportal.hr_management_api.department;

import jakarta.validation.constraints.NotBlank;

public record DepartmentDto(@NotBlank String name, String description) {}

