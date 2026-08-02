package com.tastyhouse.infrastructure.notice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<NoticeJpaEntity, Long> {
}
