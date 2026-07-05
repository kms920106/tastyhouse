package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.model.WithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record WithdrawMemberCommand(
    MemberId memberId,
    WithdrawalReason reason,
    String reasonDetail
) {

    public static WithdrawMemberCommand of(MemberId memberId, WithdrawalReason reason, String reasonDetail) {
        return new WithdrawMemberCommand(memberId, reason, reasonDetail);
    }
}
