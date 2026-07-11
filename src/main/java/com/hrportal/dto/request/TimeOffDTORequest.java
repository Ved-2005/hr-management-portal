package com.hrportal.dto.request;

import java.time.LocalDate;

import com.hrportal.type.LeaveType;

public record TimeOffDTORequest(
    LeaveType leaveType,
    LocalDate startDate,
    LocalDate endDate,
    String reason
) {}
