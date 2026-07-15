package com.tastyhouse.core.domain.member.referral.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.member.referral.domain.vo.ReferralId;

public interface MemberReferralRepository {

    boolean existsByRefereeId(MemberId refereeId);

    List<MemberReferral> findByReferrerId(MemberId referrerId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
