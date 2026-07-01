package com.project.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.service.*;

@RestController
public class WhatsAppController {

    @Autowired
    private WhatsAppService service;

    @GetMapping("/send")
    public String send() {
        service.sendMessage(
                "9822842422",   // Receiver number
                "Hello from Spring Boot!"
        );
        return "Message Sent";
    }

    @org.springframework.web.bind.annotation.PostMapping("/send-whatsapp-payslip")
    public String sendPayslip(
            @org.springframework.web.bind.annotation.RequestParam("employeeName") String employeeName,
            @org.springframework.web.bind.annotation.RequestParam("phoneNumber") String phoneNumber,
            @org.springframework.web.bind.annotation.RequestParam("month") String month,
            @org.springframework.web.bind.annotation.RequestParam("totalEarning") String totalEarning,
            @org.springframework.web.bind.annotation.RequestParam("totalDeduction") String totalDeduction,
            @org.springframework.web.bind.annotation.RequestParam("netPayment") String netPayment) {

        String payslipText = String.format(
            "*Payslip Details*\n\n" +
            "Employee Name: %s\n" +
            "Payment details for: %s\n" +
            "Total earning: %s\n" +
            "Total deduction: %s\n" +
            "Net payment: %s",
            employeeName,
            month,
            totalEarning,
            totalDeduction,
            netPayment
        );

        service.sendMessage(phoneNumber, payslipText);

        return "Message Sent Successfully";
    }
}