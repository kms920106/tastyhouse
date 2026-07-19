package com.tastyhouse.infrastructure.point.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPointJpaRepository extends JpaRepository<MemberPointJpaEntity, Long> {
}
