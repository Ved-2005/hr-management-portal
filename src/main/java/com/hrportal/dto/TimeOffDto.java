package com.hrportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import com.hrportal.type.LeaveType;

public record TimeOffDto(
    @NotNull LeaveType leaveType,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotBlank String reason
) {}
