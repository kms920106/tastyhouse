package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;

public interface BugReportJpaRepository extends JpaRepository<BugReport, Long> {
}
