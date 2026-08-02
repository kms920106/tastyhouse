package com.tastyhouse.domain.member.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public record MemberRegisteredEvent(
    MemberId memberId,
    String username,
    LocalDateTime registeredAt
) {
}
