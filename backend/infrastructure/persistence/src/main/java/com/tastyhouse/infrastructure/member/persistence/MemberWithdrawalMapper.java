package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberWithdrawalMapper {
    private MemberWithdrawalMapper() {
    }

    static MemberWithdrawal toDomain(MemberWithdrawalJpaEntity entity) {
        return MemberWithdrawal.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getReason(),
            entity.getReasonDetail(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberWithdrawalJpaEntity toEntity(MemberWithdrawal domain) {
        return MemberWithdrawalJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getReason(),
            domain.getReasonDetail()
        );
    }
}
