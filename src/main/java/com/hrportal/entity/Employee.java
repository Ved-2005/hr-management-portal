package com.hrportal.entity;

import com.hrportal.status.EmployeeStatus;

import com.hrportal.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee extends BaseEntity {
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[A-Za-z]{7}$", message = "Username must be exactly 7 letters")
    private String username;
    private String designation;
    private Double salary;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
}