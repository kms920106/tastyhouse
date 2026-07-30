package com.tastyhouse.infrastructure.rank.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 진행 중인 랭킹의 등수별 경품 조회 결과 — web 랭킹 화면이 소비한다.
 *
 * <p>표시에 필요한 파일 경로만 포함한다. 관리 화면용 {@link RankPrizeManagementResult}는 파일 ID·원본
 * 파일명까지 필요해 필드 셋이 다르므로 통합하지 않는다(과잉 노출 방지).
 */
public record RankPrizeResult(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageFilePath
) {
    @QueryProjection
    public RankPrizeResult {
    }
}
