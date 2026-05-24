package com.tastyhouse.core.domain.member.infrastructure.persistence;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWithdrawalJpaRepository extends JpaRepository<MemberWithdrawal, Long> {
}
