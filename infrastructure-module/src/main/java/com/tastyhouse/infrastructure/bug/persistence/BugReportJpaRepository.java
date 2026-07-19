package com.tastyhouse.infrastructure.bug.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportJpaRepository extends JpaRepository<BugReportJpaEntity, Long> {
}
