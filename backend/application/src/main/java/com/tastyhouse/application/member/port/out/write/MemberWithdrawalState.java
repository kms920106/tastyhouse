package com.tastyhouse.application.member.port.out.write;

import java.time.LocalDateTime;

public record MemberWithdrawalState(
    Long id,
    Long memberId,
    String reason,
    String reasonDetail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
