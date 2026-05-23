package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportImageJpaRepository extends JpaRepository<BugReportImage, Long> {
}
