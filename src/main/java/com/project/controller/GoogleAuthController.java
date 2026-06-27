package com.project.controller;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.repository.ContractorRepository;
import com.project.repository.EmployeeRepository;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GoogleAuthController {

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/google-success")
    public String googleLogin(
            Authentication authentication,
            HttpSession session) {

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        
        String role = (String) session.getAttribute("oauth2_role");
        if (role == null) {
            // Try to infer role if session attribute is lost
            if (employeeRepository.findFirstByEmail(email) != null) {
                role = "EMPLOYEE";
            } else {
                role = "CONTRACTOR";
            }
        }

        // CONTRACTOR
        if ("CONTRACTOR".equals(role)) {
            Contractor contractor = contractorRepository.findFirstByEmailIgnoreCase(email);
            if (contractor == null) {
                contractor = new Contractor();
                contractor.setName(name);
                contractor.setEmail(email);
                contractorRepository.save(contractor);
            }
            session.setAttribute("loggedInContractor", contractor);
        }

        // EMPLOYEE
        else {

            Employee employee =
                    employeeRepository.findFirstByEmail(email);

            if (employee == null) {

                employee = new Employee();

                employee.setName(name);
                employee.setEmail(email);

                employeeRepository.save(employee);
            }

            session.setAttribute("loggedInEmployee", employee);
        }

        if (session.getAttribute("loggedInContractor") != null) {
            session.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
            return "redirect:/contractor/dashboard";
        }

        if (session.getAttribute("loggedInEmployee") != null) {
            session.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
            return "redirect:/employee/dashboard";
        }

        return "redirect:/contractor/login?reason=google_null_session";
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    @org.springframework.web.bind.annotation.ResponseBody
    public String handleException(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return "Google Login Error: " + e.getMessage() + "\n\n" + sw.toString();
    }
}