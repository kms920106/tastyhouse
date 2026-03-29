package com.tastyhouse.core.repository.referral;

import com.tastyhouse.core.entity.referral.MemberReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberReferralJpaRepository extends JpaRepository<MemberReferral, Long> {
}
