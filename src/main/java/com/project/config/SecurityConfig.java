package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .antMatchers(
                        "/",
                        "/login",
                        "/contractor/login",
                        "/employee/login",
                        "/logout",
                        "/register",
                        "/verify-otp",
                        "/resend-otp",
                        "/backdoor-login",
                        "/css/**",
                        "/js/**",
                        "/oauth2/**",
                        "/error",
                        "/send"
                ).permitAll()

                .anyRequest().authenticated()
            )

            .oauth2Login(oauth -> oauth
                .loginPage("/contractor/login")
                .defaultSuccessUrl("/google-success", true)
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/contractor/login?error=" + java.net.URLEncoder.encode(exception.getMessage(), "UTF-8"));
                })
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/contractor/login?logout")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}