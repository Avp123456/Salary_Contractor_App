package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.entity.ReportConfigurationFile;
import java.util.List;

public interface ReportConfigurationFileRepository extends JpaRepository<ReportConfigurationFile, Long> {
    List<ReportConfigurationFile> findByConfigId(Long configId);
    void deleteByConfigId(Long configId);
}
