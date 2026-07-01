package com.tastyhouse.core.domain.point.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;

public interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, Long> {
}
