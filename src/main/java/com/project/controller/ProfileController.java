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

        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) {
            return "redirect:/login";
        }

        Contractor contractor = userService.findByEmail(sessionContractor.getEmail());

        model.addAttribute("user", contractor);

        return "contractor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name, @RequestParam String mobile, HttpSession session) {

        Contractor sessionContractor = (Contractor) session.getAttribute("loggedInContractor");
        if (sessionContractor == null) {
            return "redirect:/login";
        }

        Contractor contractor = userService.findByEmail(sessionContractor.getEmail());

        contractor.setName(name);
        contractor.setMobile(mobile);

        userService.save(contractor);
        
        session.setAttribute("loggedInContractor", contractor);

        return "redirect:/contractor/profile";
    }
}