package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportJpaRepository extends JpaRepository<BugReport, Long> {
}
