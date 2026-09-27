package com.tastyhouse.infrastructure.member.referral.persistence;

import com.tastyhouse.application.member.referral.port.out.write.MemberReferralState;

final class MemberReferralMapper {
    private MemberReferralMapper() {
    }

    static MemberReferralState toState(MemberReferralJpaEntity entity) {
        return new MemberReferralState(
            entity.getId(),
            entity.getReferrerId(),
            entity.getRefereeId(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }

    static MemberReferralJpaEntity toEntity(MemberReferralState state) {
        return MemberReferralJpaEntity.create(
            state.referrerId(),
            state.refereeId(),
            state.status()
        );
    }

    static void applyChanges(MemberReferralJpaEntity entity, MemberReferralState state) {
        entity.applyChanges(state.status());
    }
}
