package com.hrportal.dto.response;

import com.hrportal.entity.Department;

public record DepartmentDTOResponse(
    String name,
    String description,
    boolean active
) {
    public static DepartmentDTOResponse toDepartmentDTOResponse(Department dept) {
        if (dept == null) {
            return null;
        }
        return new DepartmentDTOResponse(
            dept.getName(),
            dept.getDescription(),
            dept.isActive()
        );
    }
}
