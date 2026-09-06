package com.tastyhouse.domain.member.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;

public record MemberWithdrawnEvent(
    MemberId memberId,
    MemberWithdrawalReason reason,
    LocalDateTime withdrawnAt
) {
}
