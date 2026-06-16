package com.hrportal.dto;

import com.hrportal.status.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDto(
    @NotBlank String username,
    @NotBlank String password,
    @NotNull Role role 
) {}