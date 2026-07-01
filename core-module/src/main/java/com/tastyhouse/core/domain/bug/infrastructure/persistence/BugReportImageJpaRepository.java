package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;

public interface BugReportImageJpaRepository extends JpaRepository<BugReportImage, Long> {
}
