package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.service.EmployeeService;

import javax.servlet.http.HttpSession;
@Controller
public class EmployeesController {
	 //time stamp
    private String getTime() {
        return java.time.LocalDateTime.now().toString();
    }
    
    @Autowired
    private EmployeeService employeeService;
    
   


    @GetMapping("/contractor/employees")
    public String employees(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Employees");
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        List<Employee> list = employeeService.getByContractor(contractor);
        model.addAttribute("employees", list);
        System.out.println("[INFO] employees page visited "+getTime());
        
        return "contractor/employees";
    }

    @GetMapping("/contractor/add-employee")
    public String addEmployeePage(@org.springframework.web.bind.annotation.RequestParam(required = false) String empCode,
                                  @org.springframework.web.bind.annotation.RequestParam(required = false) String name,
                                  @org.springframework.web.bind.annotation.RequestParam(required = false) String source,
                                  Model model) {
        System.out.println("[Page Visited]:- Add Employee");
        Employee emp = new Employee();
        if (empCode != null) emp.setEmpCode(empCode);
        if (name != null) emp.setName(name);
        model.addAttribute("employee", emp);
        if (source != null) model.addAttribute("source", source);
        System.out.println("[ACTION] Add Employee button Clicked "+getTime());

        return "contractor/add-employee";
    }

    @PostMapping("/contractor/save-employee")
    public String saveEmployee(@ModelAttribute Employee employee,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        System.out.println("[Button clicked]:- Save Employee");
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");

        if (employee.getEmployeeId() == null) {
            // ✅ New Employee Logic
            if (employeeService.empCodeExists(employee.getEmpCode())) {
                redirectAttributes.addFlashAttribute("error", "Employee ID already exists!");
                return "redirect:/contractor/add-employee";
            }

            if (employeeService.emailExists(employee.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email already exists!");
                return "redirect:/contractor/add-employee";
            }

            if (employeeService.mobileExists(employee.getMobileNo())) {
                redirectAttributes.addFlashAttribute("error", "Mobile number already exists!");
                return "redirect:/contractor/add-employee";
            }
            
            // Assign current contractor to new employee
            employee.setContractor(contractor);
        } else {
            // ✅ Edit Employee Logic
            Employee existing = employeeService.getById(employee.getEmployeeId());
            if (existing != null) {
                // Check duplicate email (excluding current employee)
                if (employeeService.emailExistsForOther(employee.getEmail(), employee.getEmployeeId())) {
                    redirectAttributes.addFlashAttribute("error", "Email already exists!");
                    return "redirect:/contractor/edit-employee/" + employee.getEmployeeId();
                }

                // Check duplicate mobile (excluding current employee)
                if (employeeService.mobileExistsForOther(employee.getMobileNo(), employee.getEmployeeId())) {
                    redirectAttributes.addFlashAttribute("error", "Mobile number already exists!");
                    return "redirect:/contractor/edit-employee/" + employee.getEmployeeId();
                }

                // Preserve fields not present in edit form (contractor, password, original empCode)
                employee.setContractor(existing.getContractor());
                employee.setPassword(existing.getPassword());
                employee.setEmpCode(existing.getEmpCode());
            }
        }

        employeeService.save(employee);
        System.out.println("[ACTION] Employee saved/updated: " + employee.getName() + " " + getTime());
        return "redirect:/contractor/employees";
    }

    @GetMapping("/contractor/delete-employee/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        System.out.println("[Button clicked]:- Delete Employee");
        employeeService.deleteById(id);
        System.out.println("[ACTION] Delete button Clicked "+getTime());
        return "redirect:/contractor/employees";
    }

    @GetMapping("/contractor/edit-employee/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        System.out.println("[Button clicked]:- Edit Employee");
        Employee emp = employeeService.getById(id);
        model.addAttribute("employee", emp);
        System.out.println("[ACTION] Edit button Clicked "+getTime());

        return "contractor/add-employee";
    }

}
