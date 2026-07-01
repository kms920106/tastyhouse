package com.tastyhouse.core.domain.member.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.model.WithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberWithdrawnEvent(
    MemberId memberId,
    WithdrawalReason reason,
    LocalDateTime withdrawnAt
) {
}
