package com.tastyhouse.core.repository.referral;

import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.referral.ReferralStatus;

import java.util.List;
import java.util.Optional;

public interface MemberReferralRepository {

    boolean existsByRefereeId(Long refereeId);

    List<MemberReferral> findByReferrerId(Long referrerId);

    List<MemberReferral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    Optional<MemberReferral> findByRefereeId(Long refereeId);
}
