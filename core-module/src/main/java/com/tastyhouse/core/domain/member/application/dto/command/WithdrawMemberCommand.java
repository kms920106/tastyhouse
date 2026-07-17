package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record WithdrawMemberCommand(
    MemberId memberId,
    MemberWithdrawalReason reason,
    String reasonDetail
) {

    public static WithdrawMemberCommand of(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        return new WithdrawMemberCommand(memberId, reason, reasonDetail);
    }
}
