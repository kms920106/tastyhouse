package com.tastyhouse.domain.member.referral.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;

public interface MemberReferralRepository {
    boolean existsByRefereeId(MemberId refereeId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
