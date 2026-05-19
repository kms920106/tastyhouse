package com.tastyhouse.core.domain.notice.infrastructure.persistence;

import com.tastyhouse.core.domain.notice.domain.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {
}
