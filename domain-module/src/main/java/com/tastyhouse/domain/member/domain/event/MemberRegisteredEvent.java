package com.tastyhouse.domain.member.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;

public record MemberRegisteredEvent(
    MemberId memberId,
    String username,
    LocalDateTime registeredAt
) {
}
