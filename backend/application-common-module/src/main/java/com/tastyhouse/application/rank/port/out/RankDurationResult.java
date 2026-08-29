package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;

/**
 * 현재 진행 중인 랭킹의 기간(시작·종료 일시) — web 랭킹 화면 헤더가 소비한다.
 */
public record RankDurationResult(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
