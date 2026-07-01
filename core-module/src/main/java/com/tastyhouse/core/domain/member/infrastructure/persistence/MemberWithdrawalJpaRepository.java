package com.tastyhouse.core.domain.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawal;

@Repository
public interface MemberWithdrawalJpaRepository extends JpaRepository<MemberWithdrawal, Long> {
}
