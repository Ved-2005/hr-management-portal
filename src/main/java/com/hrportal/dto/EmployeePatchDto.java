package com.hrportal.dto;

import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record EmployeePatchDto(
    String firstName,
    String lastName,
    String username,
    String designation,
    @Positive Double salary,
    Long departmentId
) {}
