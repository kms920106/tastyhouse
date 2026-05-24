package com.tastyhouse.core.domain.referral.domain.repository;

import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;
import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;

import java.util.List;
import java.util.Optional;

public interface MemberReferralRepository {

    boolean existsByRefereeId(Long refereeId);

    List<MemberReferral> findByReferrerId(Long referrerId);

    List<MemberReferral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    Optional<MemberReferral> findByRefereeId(Long refereeId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
