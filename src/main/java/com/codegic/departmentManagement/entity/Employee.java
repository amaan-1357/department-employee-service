package com.codegic.departmentManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @DecimalMin(value = "0.01", message = "Salary must be greater than 0")
    @Column(nullable = false)
    private BigDecimal salary;

    @PastOrPresent(message = "Joining date cannot be in the future")
    @Column(nullable = false)
    private LocalDate joiningDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
