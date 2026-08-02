package com.tastyhouse.infrastructure.rank.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 현재 진행 중인 랭킹의 기간(시작·종료 일시) — web 랭킹 화면 헤더가 소비한다.
 */
public record RankDurationResult(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    @QueryProjection
    public RankDurationResult {
    }
}
