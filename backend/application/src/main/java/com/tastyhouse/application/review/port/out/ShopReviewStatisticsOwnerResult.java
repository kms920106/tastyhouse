package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Map;

/**
 * 점주 리뷰 대시보드 집계 — 게이트 판정 결과와 6개월 집계 전부.
 *
 * <p><b>챕터 09</b>에서 신설. 이 응답은 조회 포트를 <b>여덟 번 이상</b> 호출해 합친 것이고
 * (게이트 카운트·기간 카운트·재방문 카운트·카테고리 평균·별점 분포·월별 2종 등), 게이트를 통과하지
 * 못하면 나머지 쿼리를 아예 실행하지 않는 <b>순서 있는 판정</b>이 들어 있다. 표현 계약이 대신할 수
 * 없으므로 합친 결과를 나른다.
 *
 * <p><b>이름에 {@code Owner} 한정어가 붙은 이유</b>: 같은 계약 패키지에 web 소비용
 * {@code ShopReviewStatisticsResult}가 <b>이미 있고 필드 구성이 다르다</b>(게이트 플래그 없음·월별
 * 형태 다름). 형제가 이미 순수명을 점유했으므로 소유 주체를 담은 {@code Owner}를 붙였다(리포 규약:
 * admin이 {@code Management}를 점유했으면 ceo는 {@code Owner}). 챕터 04로 이 패키지는
 * {@code application} 한 모듈이 단독 소유하므로, 이름이 겹치면 컴파일 에러로 즉시 드러난다.
 *
 * <p>{@code hasData}가 {@code false}면 나머지는 전부 비어 있다 — 최근 180일 리뷰가 0건이면 대시보드를
 * 노출하지 않는다는 원문 규격이며, 그 빈 형태는 {@code ShopReviewStatisticsResponse#empty}가 만든다.
 */
public record ShopReviewStatisticsOwnerResult(
    boolean hasData,
    Double averageTotalRating,
    Long totalReviewCount,
    Long recentReviewCount,
    Map<Integer, Long> ratingCounts,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating,
    Double averageAtmosphereRating,
    Double averageKindnessRating,
    Double averageHygieneRating,
    Double willRevisitPercentage,
    List<ShopReviewMonthlyStatResult> monthlyStats
) {
}
