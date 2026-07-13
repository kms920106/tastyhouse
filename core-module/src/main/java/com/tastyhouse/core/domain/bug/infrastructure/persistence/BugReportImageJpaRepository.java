package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;

public interface BugReportImageJpaRepository extends JpaRepository<BugReportImage, Long> {

    List<BugReportImage> findByBugReportIdOrderBySortAsc(Long bugReportId);
}
