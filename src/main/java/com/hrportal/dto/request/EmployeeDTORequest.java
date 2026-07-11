package com.hrportal.dto.request;

public record EmployeeDTORequest(
    String firstName,
    String lastName,
    String username,
    String designation,
    Double salary,
    Long departmentId
) {}
