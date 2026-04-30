package com.project.service;

import com.project.entity.OtpData;
import com.project.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final int OTP_EXPIRY_MINUTES = 5;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Transactional
    public void sendAndSaveOtp(String name, String email) {
        String otp = generateOtp();
        
        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(email);
        
        // Save new OTP
        OtpData otpData = new OtpData(email, otp, LocalDateTime.now());
        otpRepository.save(otpData);
        
        // Send HTML Email with refined design
        try {
            sendPremiumHtmlEmail(name, email, otp);
            System.out.println("[INFO] OTP Sent to " + email + ": " + otp);
        } catch (MessagingException e) {
            System.err.println("[ERROR] Failed to send OTP email: " + e.getMessage());
            throw new RuntimeException("Email sending failed");
        }
    }

    private void sendPremiumHtmlEmail(String name, String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Action Required: Verify Your Registration");

        String htmlContent = 
            "<div style='background-color: #0f172a; padding: 40px 0; font-family: \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);'>" +
                    
                    // TOP BAR & HEADER
                    "<div style='background-color: #eff6ff; padding: 40px 20px; text-align: center; border-bottom: 1px solid #e2e8f0;'>" +
                        "<div style='display: inline-block; padding: 10px 20px; border: 2px solid #2563eb; border-radius: 8px;'>" +
                            "<span style='font-size: 24px; font-weight: 800; color: #1e40af; letter-spacing: 1px;'>Salary Contractor App</span>" +
                        "</div>" +
                    "</div>" +

                    // CONTENT
                    "<div style='padding: 40px;'>" +
                        "<h2 style='color: #1e293b; font-size: 22px; font-weight: 700; margin-bottom: 20px;'>Dear " + name + ",</h2>" +
                        "<p style='color: #475569; font-size: 16px; line-height: 1.6; margin-bottom: 30px;'>" +
                            "To ensure the security of your account with <b>Salary Contractor App</b>, please use the verification code below to complete your setup." +
                        "</p>" +

                        // OTP CARD
                        "<div style='border: 1px solid #bfdbfe; border-radius: 12px; overflow: hidden;'>" +
                            "<div style='background-color: #2563eb; padding: 15px 20px;'>" +
                                "<h3 style='color: #ffffff; font-size: 16px; font-weight: 600; margin: 0;'>Verification Step: Enter One-Time Password</h3>" +
                            "</div>" +
                            "<div style='padding: 30px; background-color: #f8fafc; text-align: center;'>" +
                                "<div style='font-size: 42px; font-weight: 800; color: #1e3a8a; letter-spacing: 12px; margin-bottom: 10px;'>" + otp + "</div>" +
                                "<p style='color: #64748b; font-size: 14px; margin: 0;'>" +
                                    "Use this OTP to complete your Registration Process. It is Valid for 5 minuts only" +
                                "</p>" +
                            "</div>" +
                        "</div>" +

                        "<p style='color: #94a3b8; font-size: 13px; margin-top: 30px; line-height: 1.5;'>" +
                            "If this Registration was not requested by you, please reach out to our support team. Never Share OTP with anyone" +
                        "</p>" +
                    "</div>" +

                    // FOOTER
                    "<div style='background-color: #f1f5f9; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>" +
                        "<p style='color: #64748b; font-size: 12px; margin: 0;'>&copy; 2026 Salary Contractor App. All rights reserved.</p>" +
                        "<p style='color: #94a3b8; font-size: 11px; margin: 5px 0 0 0;'>You are receiving this email because you signed up for an account.</p>" +
                    "</div>" +
                "</div>" +
            "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public boolean validateOtp(String email, String userOtp) {
        Optional<OtpData> otpDataOpt = otpRepository.findByEmail(email);
        
        if (otpDataOpt.isPresent()) {
            OtpData otpData = otpDataOpt.get();
            
            // Check expiry
            if (otpData.getGeneratedTime().plusMinutes(OTP_EXPIRY_MINUTES).isAfter(LocalDateTime.now())) {
                return otpData.getOtp().equals(userOtp);
            }
        }
        return false;
    }
}
