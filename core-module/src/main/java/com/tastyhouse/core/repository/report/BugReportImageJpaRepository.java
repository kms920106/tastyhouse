package com.tastyhouse.core.repository.report;

import com.tastyhouse.core.entity.report.BugReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BugReportImageJpaRepository extends JpaRepository<BugReportImage, Long> {
}
