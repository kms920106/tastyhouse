package com.tastyhouse.application.rank.port.out.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberReviewRankState(
    Long id,
    Long memberId,
    Integer reviewCount,
    Integer rankNo,
    String rankType,
    LocalDate baseDate,
    LocalDateTime lastReviewAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
