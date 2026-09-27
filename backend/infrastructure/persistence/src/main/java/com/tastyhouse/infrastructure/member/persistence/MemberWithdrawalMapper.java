package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.application.member.port.out.write.MemberWithdrawalState;

final class MemberWithdrawalMapper {
    private MemberWithdrawalMapper() {
    }

    static MemberWithdrawalState toState(MemberWithdrawalJpaEntity entity) {
        return new MemberWithdrawalState(
            entity.getId(),
            entity.getMemberId(),
            entity.getReason(),
            entity.getReasonDetail(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberWithdrawalJpaEntity toEntity(MemberWithdrawalState state) {
        return MemberWithdrawalJpaEntity.create(
            state.memberId(),
            state.reason(),
            state.reasonDetail()
        );
    }
}
