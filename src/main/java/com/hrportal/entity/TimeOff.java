package com.hrportal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

import com.hrportal.common.BaseEntity;
import com.hrportal.status.LeaveStatus;
import com.hrportal.type.LeaveType;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimeOff extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;


    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotBlank
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
}
