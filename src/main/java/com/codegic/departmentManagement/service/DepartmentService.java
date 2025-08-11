package com.codegic.departmentManagement.service;

import com.codegic.departmentManagement.dto.DepartmentRequest;
import com.codegic.departmentManagement.dto.DepartmentResponse;
import com.codegic.departmentManagement.entity.Department;
import com.codegic.departmentManagement.entity.Employee;
import com.codegic.departmentManagement.repository.DepartmentRepository;
import com.codegic.departmentManagement.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .build();
        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        return mapToResponse(department);
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        department.setName(request.getName());
        department.setCode(request.getCode());
        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    public void deleteDepartment(Long id, boolean force) {
        if (!departmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Department not found");
        }

        List<Employee> employees = employeeRepository.findByDepartmentId(id);

        if (!employees.isEmpty()) {
            if (!force) {
                throw new IllegalStateException(
                        "Cannot delete department with existing employees. Use ?force=true to delete anyway."
                );
            }

            // Force delete: log and remove employees first
            employees.forEach(emp ->
                    log.info("Force delete: Removing employee ID={} Name={} Email={}",
                            emp.getId(), emp.getName(), emp.getEmail())
            );
            employeeRepository.deleteAll(employees);
        }

        log.info("Deleting department ID={}", id);
        departmentRepository.deleteById(id);
    }



    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }
}