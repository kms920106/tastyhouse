package com.tastyhouse.infrastructure.jpa.bug.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface BugReportJpaRepository extends JpaRepository<BugReportJpaEntity, Long> {
}
