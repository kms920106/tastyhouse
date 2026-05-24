package com.tastyhouse.core.domain.member.domain.event;

import com.tastyhouse.core.domain.member.domain.model.WithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

import java.time.LocalDateTime;

public record MemberWithdrawnEvent(
    MemberId memberId,
    WithdrawalReason reason,
    LocalDateTime withdrawnAt
) {
}
