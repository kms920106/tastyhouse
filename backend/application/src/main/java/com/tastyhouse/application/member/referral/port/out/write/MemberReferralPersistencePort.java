package com.tastyhouse.application.member.referral.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberReferralPersistencePort {

    boolean existsByRefereeId(MemberId refereeId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
