package com.hrportal.dto.response;

import com.hrportal.entity.TimeOff;
import com.hrportal.status.LeaveStatus;
import com.hrportal.type.LeaveType;

import java.time.LocalDate;

public record TimeOffDTOResponse(
    Long id,
    String employeeUsername,
    LeaveType leaveType,
    LocalDate startDate,
    LocalDate endDate,
    String reason,
    LeaveStatus status
) {
    public static TimeOffDTOResponse toTimeOffDTOResponse(TimeOff timeOff) {
        if (timeOff == null) {
            return null;
        }
        String employeeUsername = (timeOff.getEmployee() != null) ? timeOff.getEmployee().getUsername() : null;
        return new TimeOffDTOResponse(
            timeOff.getId(),
            employeeUsername,
            timeOff.getLeaveType(),
            timeOff.getStartDate(),
            timeOff.getEndDate(),
            timeOff.getReason(),
            timeOff.getStatus()
        );
    }
}
