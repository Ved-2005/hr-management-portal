package com.hrportal.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;

import com.hrportal.common.BaseEntity;

import jakarta.persistence.Column;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder 
public class Department extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @Default private boolean active = true;
    private Integer sickLeaves;
    private Integer casualLeaves;
    private Integer paidLeaves;
}