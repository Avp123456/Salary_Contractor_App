package com.project.entity;

import javax.persistence.*;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    private String name;
    private String email;
    private String password;
    private String empCode;

    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

    // ✅ DEFAULT CONSTRUCTOR (MANDATORY)
    public Employee() {
    }

    // Optional: parameterized constructor
    public Employee(String name, String email, String password, Contractor contractor) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contractor = contractor;
    }

    // Getters & Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }
}