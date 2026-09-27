package com.tastyhouse.application.member.store;

import com.tastyhouse.application.member.port.out.write.MemberWithdrawalState;
import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberWithdrawalStateMapper {
    private MemberWithdrawalStateMapper() {
    }

    static MemberWithdrawal toDomain(MemberWithdrawalState state) {
        return MemberWithdrawal.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.reason() == null ? null : MemberWithdrawalReason.valueOf(state.reason()),
            state.reasonDetail(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MemberWithdrawalState toState(MemberWithdrawal withdrawal) {
        return new MemberWithdrawalState(
            withdrawal.getId(),
            withdrawal.getMemberId() == null ? null : withdrawal.getMemberId().value(),
            withdrawal.getReason() == null ? null : withdrawal.getReason().name(),
            withdrawal.getReasonDetail(),
            withdrawal.getCreatedAt(),
            withdrawal.getUpdatedAt()
        );
    }
}
