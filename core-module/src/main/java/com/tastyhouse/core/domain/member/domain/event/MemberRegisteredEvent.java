package com.tastyhouse.core.domain.member.domain.event;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

import java.time.LocalDateTime;

public record MemberRegisteredEvent(
    MemberId memberId,
    String username,
    LocalDateTime registeredAt
) {
}
