package com.project.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_handler")
public class OtpData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String otp;
    private LocalDateTime generatedTime;

    public OtpData() {}

    public OtpData(String email, String otp, LocalDateTime generatedTime) {
        this.email = email;
        this.otp = otp;
        this.generatedTime = generatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }

    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }
}
