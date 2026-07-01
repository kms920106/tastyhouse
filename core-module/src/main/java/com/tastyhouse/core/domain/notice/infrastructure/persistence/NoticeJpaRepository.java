package com.tastyhouse.core.domain.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.notice.domain.model.Notice;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {
}
