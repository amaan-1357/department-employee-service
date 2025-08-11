package com.codegic.departmentManagement.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private String email;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private Long departmentId;
    private String departmentName;
}