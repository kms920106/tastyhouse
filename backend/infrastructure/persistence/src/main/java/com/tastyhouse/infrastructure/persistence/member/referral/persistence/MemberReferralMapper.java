package com.tastyhouse.infrastructure.persistence.member.referral.persistence;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberReferralMapper {

    private MemberReferralMapper() {
    }

    static MemberReferral toDomain(MemberReferralJpaEntity entity) {
        return MemberReferral.reconstitute(
            entity.getId(),
            entity.getReferrerId() == null ? null : MemberId.of(entity.getReferrerId()),
            entity.getRefereeId() == null ? null : MemberId.of(entity.getRefereeId()),
            entity.getStatus() == null ? null : MemberReferralStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt()
        );
    }

    static MemberReferralJpaEntity toEntity(MemberReferral referral) {
        return MemberReferralJpaEntity.create(
            referral.getReferrerId() == null ? null : referral.getReferrerId().value(),
            referral.getRefereeId() == null ? null : referral.getRefereeId().value(),
            referral.getStatus() == null ? null : referral.getStatus().name()
        );
    }

    static void applyChanges(MemberReferralJpaEntity entity, MemberReferral referral) {
        entity.applyChanges(referral.getStatus() == null ? null : referral.getStatus().name());
    }
}
