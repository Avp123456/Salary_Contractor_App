package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entity.Contractor;
import com.project.service.ContractorService;

import javax.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @Autowired
    private ContractorService userService;

    @GetMapping("/contractor/profile")
    public String profile(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Profile");
        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) {
            return "redirect:/contractor/login";
        }

        Contractor contractor = userService.findByEmail(sessionContractor.getEmail());

        model.addAttribute("user", contractor);

        return "contractor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name, @RequestParam String mobile, HttpSession session) {
        System.out.println("[Button clicked]:- Update Profile");
        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) {
            return "redirect:/contractor/login";
        }

        Contractor contractor = userService.findByEmail(sessionContractor.getEmail());

        contractor.setName(name);
        contractor.setMobile(mobile);

        userService.save(contractor);
        
        session.setAttribute("loggedInContractor", contractor);

        return "redirect:/contractor/profile";
    }

    @GetMapping("/contractor/change-password")
    public String changePasswordPage(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Change Password");
        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) {
            return "redirect:/contractor/login";
        }
        Contractor contractor = userService.findByEmail(sessionContractor.getEmail());
        model.addAttribute("contractor", contractor);
        return "contractor/change-password";
    }

    @PostMapping("/contractor/update-password")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> updatePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, HttpSession session) {
        System.out.println("[Button clicked]:- Update Password");
        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("status", "error", "message", "Unauthorized"));

        Contractor dbContractor = userService.findByEmail(sessionContractor.getEmail());
        if (dbContractor == null) return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("status", "error", "message", "Unauthorized"));

        if (!dbContractor.getPassword().equals(currentPassword)) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "message", "Current password is incorrect"));
        }
        
        if (currentPassword.equals(newPassword)) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "message", "New password cannot be the same as the current password"));
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "message", "New passwords do not match"));
        }

        dbContractor.setPassword(newPassword);
        userService.save(dbContractor);
        session.setAttribute("loggedInContractor", dbContractor);

        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "success", "message", "Password updated successfully", "redirect", "/contractor/profile"));
    }
}