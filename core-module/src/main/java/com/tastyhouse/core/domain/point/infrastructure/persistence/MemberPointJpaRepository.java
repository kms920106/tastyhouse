package com.tastyhouse.core.domain.point.infrastructure.persistence;

import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPointJpaRepository extends JpaRepository<MemberPoint, Long> {
}
