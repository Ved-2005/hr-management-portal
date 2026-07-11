package com.hrportal.dto.request;

public record DepartmentDTORequest(
    String name,
    String description,
    Integer sickLeaves,
    Integer casualLeaves,
    Integer paidLeaves
) {}
