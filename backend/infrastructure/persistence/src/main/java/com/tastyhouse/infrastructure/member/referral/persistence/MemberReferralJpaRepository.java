package com.tastyhouse.infrastructure.member.referral.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReferralJpaRepository extends JpaRepository<MemberReferralJpaEntity, Long> {
}
