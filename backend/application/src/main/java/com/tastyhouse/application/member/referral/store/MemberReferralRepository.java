package com.tastyhouse.application.member.referral.store;

import java.util.Optional;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberReferralRepository {
    boolean existsByRefereeId(MemberId refereeId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
