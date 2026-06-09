package com.hrportal.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveSummary {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private Integer sickLeaveBalance;
    private Integer casualLeaveBalance;
    private Integer paidLeaveBalance;
}
