package com.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;
    @Autowired
    private EmployeeRepository employeeRepository;

    // ✅ ADD THIS (MISSING METHOD)
    public Employee login(String email, String password) {
        return repo.findByEmailAndPassword(email, password);
    }

    // SAVE
    public void save(Employee employee) {
        repo.save(employee);
    }

    // GET ALL
    public List<Employee> getAll() {
        return repo.findAll();
    }
    
    
    public List<Employee> getByContractor(Contractor contractor) {
        return repo.findByContractor(contractor);
    }
        
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Employee getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public boolean emailExists(String email) {
        return !employeeRepository.findByEmail(email).isEmpty();
    }

    public boolean empCodeExists(String empCode) {
        return employeeRepository.existsByEmpCode(empCode);
    }

    public boolean emailExistsForOther(String email, Long id) {
        return !employeeRepository.findByEmailAndEmployeeIdNot(email, id).isEmpty();
    }

    public boolean mobileExists(String mobileNo) {
        return !employeeRepository.findByMobileNo(mobileNo).isEmpty();
    }

    public boolean mobileExistsForOther(String mobileNo, Long id) {
        return !employeeRepository.findByMobileNoAndEmployeeIdNot(mobileNo, id).isEmpty();
    }
}