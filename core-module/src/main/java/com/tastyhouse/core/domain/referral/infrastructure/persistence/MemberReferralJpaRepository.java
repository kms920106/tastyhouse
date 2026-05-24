package com.tastyhouse.core.domain.referral.infrastructure.persistence;

import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReferralJpaRepository extends JpaRepository<MemberReferral, Long> {
}
