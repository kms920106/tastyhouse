package com.tastyhouse.core.domain.point.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.point.domain.model.MemberPoint;

public interface MemberPointJpaRepository extends JpaRepository<MemberPoint, Long> {
}
