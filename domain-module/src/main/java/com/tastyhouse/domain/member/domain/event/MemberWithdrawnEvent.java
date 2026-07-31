package com.tastyhouse.domain.member.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.domain.vo.MemberId;

public record MemberWithdrawnEvent(
    MemberId memberId,
    MemberWithdrawalReason reason,
    LocalDateTime withdrawnAt
) {
}
