package com.hrportal.dto.response;

import com.hrportal.entity.Employee;
import com.hrportal.status.EmployeeStatus;

public record EmployeeDTOResponse(
    String firstName,
    String lastName,
    String username,
    String designation,
    EmployeeStatus status,
    String departmentName
) {
    public static EmployeeDTOResponse toEmployeeDTOResponse(Employee emp) {
        if (emp == null) {
            return null;
        }
        String departmentName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : null;
        return new EmployeeDTOResponse(
            emp.getFirstName(),
            emp.getLastName(),
            emp.getUsername(),
            emp.getDesignation(),
            emp.getStatus(),
            departmentName
        );
    }
}
