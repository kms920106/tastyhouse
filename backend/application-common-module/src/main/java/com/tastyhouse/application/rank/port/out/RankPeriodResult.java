package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;

/**
 * 랭킹 기간 관리 목록·상세 조회 결과 — admin 기간 관리 화면이 소비한다.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다(CLAUDE.md admin 네이밍 규칙).
 */
public record RankPeriodResult(
    Long id,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
