package com.tastyhouse.infrastructure.persistence.member.persistence;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberWithdrawalMapper {

    private MemberWithdrawalMapper() {
    }

    static MemberWithdrawal toDomain(MemberWithdrawalJpaEntity entity) {
        return MemberWithdrawal.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getReason() == null ? null : MemberWithdrawalReason.valueOf(entity.getReason()),
            entity.getReasonDetail(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberWithdrawalJpaEntity toEntity(MemberWithdrawal withdrawal) {
        return MemberWithdrawalJpaEntity.create(
            withdrawal.getMemberId() == null ? null : withdrawal.getMemberId().value(),
            withdrawal.getReason() == null ? null : withdrawal.getReason().name(),
            withdrawal.getReasonDetail()
        );
    }
}
