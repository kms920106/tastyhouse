package com.tastyhouse.core.domain.member.referral.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.member.referral.domain.model.MemberReferral;

public interface MemberReferralJpaRepository extends JpaRepository<MemberReferral, Long> {
}
