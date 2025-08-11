package com.codegic.departmentManagement.service;

import com.codegic.departmentManagement.dto.*;
import com.codegic.departmentManagement.entity.Department;
import com.codegic.departmentManagement.entity.Employee;
import com.codegic.departmentManagement.repository.DepartmentRepository;
import com.codegic.departmentManagement.repository.EmployeeRepository;
import com.codegic.departmentManagement.util.IdempotencyStore;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final IdempotencyStore idempotencyStore;

    // CRUD
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        Employee employee = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .salary(request.getSalary())
                .joiningDate(request.getJoiningDate())
                .department(department)
                .build();

        return mapToResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return mapToResponse(emp);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setSalary(request.getSalary());
        emp.setJoiningDate(request.getJoiningDate());
        emp.setDepartment(department);

        return mapToResponse(employeeRepository.save(emp));
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Salary Adjustment
    public void adjustSalaries(SalaryAdjustmentRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        if (!idempotencyStore.canAdjust(department.getId())) {
            throw new IllegalStateException("Salary adjustment already performed within last 30 minutes");
        }

        List<Employee> employees = employeeRepository.findByDepartmentId(department.getId());

        for (Employee emp : employees) {
            BigDecimal newSalary = calculateNewSalary(emp, request.getPerformanceScore());

            if (newSalary.compareTo(emp.getSalary()) > 0) {
                emp.setSalary(newSalary);
                employeeRepository.save(emp);
                log.info("Salary updated for employee {}: {}", emp.getId(), newSalary);
            } else {
                log.warn("No salary increase for employee {} due to performance score < 70", emp.getId());
            }
        }
    }

    private BigDecimal calculateNewSalary(Employee emp, int performanceScore) {
        BigDecimal currentSalary = emp.getSalary();
        BigDecimal increasePercent = BigDecimal.ZERO;

        // Performance-based adjustment
        if (performanceScore >= 90) {
            increasePercent = increasePercent.add(BigDecimal.valueOf(15));
        } else if (performanceScore >= 70) {
            increasePercent = increasePercent.add(BigDecimal.valueOf(10));
        }

        // Tenure bonus (> 5 years)
        long years = ChronoUnit.YEARS.between(emp.getJoiningDate(), LocalDate.now());
        if (years > 5) {
            increasePercent = increasePercent.add(BigDecimal.valueOf(5));
        }

        BigDecimal multiplier = BigDecimal.ONE.add(increasePercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        BigDecimal newSalary = currentSalary.multiply(multiplier);

        // Salary cap
        BigDecimal cap = BigDecimal.valueOf(200_000);
        if (newSalary.compareTo(cap) > 0) {
            return cap;
        }
        return newSalary;
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        return EmployeeResponse.builder()
                .id(emp.getId())
                .name(emp.getName())
                .email(emp.getEmail())
                .salary(emp.getSalary())
                .joiningDate(emp.getJoiningDate())
                .departmentId(emp.getDepartment().getId())
                .departmentName(emp.getDepartment().getName())
                .build();
    }
}