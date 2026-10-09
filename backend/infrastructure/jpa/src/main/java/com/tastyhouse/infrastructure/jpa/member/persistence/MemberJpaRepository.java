package com.tastyhouse.infrastructure.jpa.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
}
