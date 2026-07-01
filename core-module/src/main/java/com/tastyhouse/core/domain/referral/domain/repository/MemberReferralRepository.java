package com.tastyhouse.core.domain.referral.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;

public interface MemberReferralRepository {

    boolean existsByRefereeId(Long refereeId);

    List<MemberReferral> findByReferrerId(Long referrerId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
