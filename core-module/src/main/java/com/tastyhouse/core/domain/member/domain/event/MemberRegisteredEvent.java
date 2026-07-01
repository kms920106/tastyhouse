package com.tastyhouse.core.domain.member.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberRegisteredEvent(
    MemberId memberId,
    String username,
    LocalDateTime registeredAt
) {
}
