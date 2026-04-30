package com.project.repository;

import com.project.entity.OtpData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpData, Long> {
    Optional<OtpData> findByEmail(String email);
    void deleteByEmail(String email);
}
