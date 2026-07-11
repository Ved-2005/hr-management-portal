package com.hrportal.dto.request;

import com.hrportal.status.Role;

public record AuthDTORequest(
    String username,
    String password,
    Role role
) {}
