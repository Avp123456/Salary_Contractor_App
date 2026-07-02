package com.project.repository;

import com.project.entity.ReportConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportConfigurationRepository extends JpaRepository<ReportConfiguration, Long> {
    List<ReportConfiguration> findByContractorId(Long contractorId);
    List<ReportConfiguration> findByContractorIdAndConfigName(Long contractorId, String configName);
}
