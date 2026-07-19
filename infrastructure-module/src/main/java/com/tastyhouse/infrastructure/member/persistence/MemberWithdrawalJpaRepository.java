package com.tastyhouse.infrastructure.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWithdrawalJpaRepository extends JpaRepository<MemberWithdrawalJpaEntity, Long> {
}
