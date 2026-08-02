package com.tastyhouse.infrastructure.bug.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportImageJpaRepository extends JpaRepository<BugReportImageJpaEntity, Long> {
}
