package com.project.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.entity.Contractor;
import com.project.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByEmailAndPassword(String email, String password);
    List<Employee> findByContractor(Contractor contractor);
    List<Employee> findByEmail(String email);
    boolean existsByEmpCode(String empCode);
    List<Employee> findByEmailAndEmployeeIdNot(String email, Long employeeId);
    List<Employee> findByMobileNo(String mobileNo);
    List<Employee> findByMobileNoAndEmployeeIdNot(String mobileNo, Long employeeId);
}
