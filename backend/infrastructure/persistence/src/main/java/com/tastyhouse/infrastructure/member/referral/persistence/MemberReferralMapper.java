package com.tastyhouse.infrastructure.member.referral.persistence;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberReferralMapper {
    private MemberReferralMapper() {
    }

    static MemberReferral toDomain(MemberReferralJpaEntity entity) {
        return MemberReferral.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReferrerId(), MemberId::of),
            IdMapping.vo(entity.getRefereeId(), MemberId::of),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }

    static MemberReferralJpaEntity toEntity(MemberReferral domain) {
        return MemberReferralJpaEntity.create(
            IdMapping.raw(domain.getReferrerId(), MemberId::value),
            IdMapping.raw(domain.getRefereeId(), MemberId::value),
            domain.getStatus()
        );
    }

    static void applyChanges(MemberReferralJpaEntity entity, MemberReferral domain) {
        entity.applyChanges(domain.getStatus());
    }
}
