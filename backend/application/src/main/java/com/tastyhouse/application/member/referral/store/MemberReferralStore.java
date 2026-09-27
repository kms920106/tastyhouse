package com.tastyhouse.application.member.referral.store;

import java.util.Optional;

import com.tastyhouse.application.member.referral.port.out.write.MemberReferralStatePort;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;

public class MemberReferralStore implements MemberReferralRepository {
    private final MemberReferralStatePort memberReferralStatePort;

    public MemberReferralStore(MemberReferralStatePort memberReferralStatePort) {
        this.memberReferralStatePort = memberReferralStatePort;
    }

    @Override
    public boolean existsByRefereeId(MemberId refereeId) {
        return memberReferralStatePort.existsByRefereeId(refereeId.value());
    }

    @Override
    public Optional<MemberReferral> findById(ReferralId id) {
        return memberReferralStatePort.findById(id.value()).map(MemberReferralStateMapper::toDomain);
    }

    @Override
    public MemberReferral save(MemberReferral referral) {
        return MemberReferralStateMapper.toDomain(memberReferralStatePort.save(MemberReferralStateMapper.toState(referral)));
    }
}
