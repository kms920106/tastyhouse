package com.tastyhouse.infrastructure.bug.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportImageJpaRepository extends JpaRepository<BugReportImageJpaEntity, Long> {

    List<BugReportImageJpaEntity> findByBugReportIdOrderBySortAsc(Long bugReportId);
}
