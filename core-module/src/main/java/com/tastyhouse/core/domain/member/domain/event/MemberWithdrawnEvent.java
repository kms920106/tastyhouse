package com.tastyhouse.core.domain.member.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberWithdrawnEvent(
    MemberId memberId,
    MemberWithdrawalReason reason,
    LocalDateTime withdrawnAt
) {
}
