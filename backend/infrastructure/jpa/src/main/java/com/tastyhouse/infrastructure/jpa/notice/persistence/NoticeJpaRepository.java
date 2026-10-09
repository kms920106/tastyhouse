package com.tastyhouse.infrastructure.jpa.notice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface NoticeJpaRepository extends JpaRepository<NoticeJpaEntity, Long> {
}
