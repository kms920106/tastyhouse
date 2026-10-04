package com.tastyhouse.infrastructure.persistence.member.referral.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberReferralJpaRepository extends JpaRepository<MemberReferralJpaEntity, Long> {
}
