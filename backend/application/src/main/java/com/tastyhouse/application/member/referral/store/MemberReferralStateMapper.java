package com.tastyhouse.application.member.referral.store;

import com.tastyhouse.application.member.referral.port.out.write.MemberReferralState;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberReferralStateMapper {
    private MemberReferralStateMapper() {
    }

    static MemberReferral toDomain(MemberReferralState state) {
        return MemberReferral.reconstitute(
            state.id(),
            state.referrerId() == null ? null : MemberId.of(state.referrerId()),
            state.refereeId() == null ? null : MemberId.of(state.refereeId()),
            state.status() == null ? null : MemberReferralStatus.valueOf(state.status()),
            state.createdAt()
        );
    }

    static MemberReferralState toState(MemberReferral referral) {
        return new MemberReferralState(
            referral.getId(),
            referral.getReferrerId() == null ? null : referral.getReferrerId().value(),
            referral.getRefereeId() == null ? null : referral.getRefereeId().value(),
            referral.getStatus() == null ? null : referral.getStatus().name(),
            referral.getCreatedAt()
        );
    }
}
