package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.service.ContractorService;
import com.project.service.EmployeeService;

import javax.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private ContractorService contractorService;

	@Autowired
	private EmployeeService employeeService;

	//time stamp
	String time = java.time.LocalDateTime.now().toString();




	//login page 
	@GetMapping("/")
	public String root() {
		return "redirect:/login";
	}

	//login page 
	@GetMapping("/login")
	public String loginPage() {
		//log
		System.out.println("[INFO] Login Page Visited  "+time );

		return "login";
	}

	//log in process
	@PostMapping("/login")
	public String login(
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam String role,
			Model model,
			HttpSession session) {


		if (role.equalsIgnoreCase("CONTRACTOR")) {
			System.out.println("[ACTION] Contractor selected");

			Contractor contractor = contractorService.login(email, password);

			if (contractor != null) {
				session.setAttribute("loggedInContractor", contractor);
				//log
				System.out.println("[INFO] Contractor Logged in  "+ time);
				return "redirect:/contractor/dashboard";
			} else {
				model.addAttribute("error", "Invalid contractor credentials");

				return "login";

			}

		} else {

			Employee employee = employeeService.login(email, password);

			if (employee != null) {
				session.setAttribute("loggedInEmployee", employee);
				System.out.println("[INFO] Employee Logged in  "+ time);
				return "redirect:/employee/dashboard";
			} else {
				model.addAttribute("error", "Invalid employee credentials");
				return "login";
			}
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		System.out.println("[INFO] Contractor Logged Out  "+ time);
		return "redirect:/?logout";
	}

	@Autowired
	private com.project.service.OtpService otpService;

	@GetMapping("/register")
	public String registerPage() {
		return "register";
	}

	@PostMapping("/register")
	public String register(
			@RequestParam String name,
			@RequestParam String email,
			@RequestParam String password,
			Model model,
			HttpSession session) {

		// Check if email already exists
		if (contractorService.findByEmail(email) != null) {
			model.addAttribute("error", "Email already registered!");
			return "register";
		}

		// Store details in session for verification
		session.setAttribute("reg_name", name);
		session.setAttribute("reg_email", email);
		session.setAttribute("reg_password", password);
		session.setAttribute("otp_page_visited", false);

		try {
			otpService.sendAndSaveOtp(name, email);
		} catch (Exception e) {
			model.addAttribute("error", "Error sending OTP. Please check your email configuration.");
			return "register";
		}

		return "redirect:/verify-otp";
	}

	@GetMapping("/verify-otp")
	public String verifyOtpPage(HttpSession session, Model model) {
		String email = (String) session.getAttribute("reg_email");
		Boolean visited = (Boolean) session.getAttribute("otp_page_visited");

		if (email == null || (visited != null && visited)) {
			// Clear attributes if we are redirecting back
			session.removeAttribute("reg_name");
			session.removeAttribute("reg_email");
			session.removeAttribute("reg_password");
			session.removeAttribute("otp_page_visited");
			return "redirect:/register";
		}

		session.setAttribute("otp_page_visited", true);
		model.addAttribute("email", email);
		return "verify-otp";
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam String otp, HttpSession session, Model model) {
		String name = (String) session.getAttribute("reg_name");
		String email = (String) session.getAttribute("reg_email");
		String password = (String) session.getAttribute("reg_password");

		if (email == null) {
			return "redirect:/register";
		}

		if (otpService.validateOtp(email, otp)) {
			Contractor contractor = new Contractor();
			contractor.setName(name);
			contractor.setEmail(email);
			contractor.setPassword(password);

			contractorService.save(contractor);
			System.out.println("[ACTION] New Contractor Registered and Verified: " + email);

			// Clear session
			session.removeAttribute("reg_name");
			session.removeAttribute("reg_email");
			session.removeAttribute("reg_password");

			return "redirect:/login?registered=true";
		} else {
			model.addAttribute("error", "Invalid or expired OTP!");
			model.addAttribute("email", email);
			return "verify-otp";
		}
	}

	@GetMapping("/resend-otp")
	public String resendOtp(HttpSession session, Model model) {
		String email = (String) session.getAttribute("reg_email");
		String name = (String) session.getAttribute("reg_name");
		if (email == null) {
			return "redirect:/register";
		}

		try {
			otpService.sendAndSaveOtp(name, email);
			session.setAttribute("otp_page_visited", false);
			return "redirect:/verify-otp?resent=true";
		} catch (Exception e) {
			model.addAttribute("error", "Error resending OTP.");
			return "verify-otp";
		}
	}
}
