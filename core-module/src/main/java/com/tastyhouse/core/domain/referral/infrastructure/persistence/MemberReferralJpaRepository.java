package com.tastyhouse.core.domain.referral.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;

public interface MemberReferralJpaRepository extends JpaRepository<MemberReferral, Long> {
}
