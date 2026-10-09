package com.tastyhouse.infrastructure.jpa.member.referral.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberReferralJpaRepository extends JpaRepository<MemberReferralJpaEntity, Long> {
}
