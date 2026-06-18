package com.hrportal.auth;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {

    private Claims getClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Claims) ((UsernamePasswordAuthenticationToken) auth).getDetails();
    }

    public String getRole() {
        return getClaims().get("role", String.class);
    }

    public Long getEmployeeId() {
        return getClaims().get("employeeId", Long.class);
    }

    public Long getDepartmentId() {
        return getClaims().get("departmentId", Long.class);
    }

    public boolean isAdmin() { return "ADMIN".equals(getRole()); }
    public boolean isHR() { return "HR".equals(getRole()); }
    public boolean isEmployee() { return "EMPLOYEE".equals(getRole()); }
}