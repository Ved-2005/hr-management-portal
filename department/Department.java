package com.hrportal.hr_management_api.department;

import com.hrportal.hr_management_api.common.BaseEntity;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
}