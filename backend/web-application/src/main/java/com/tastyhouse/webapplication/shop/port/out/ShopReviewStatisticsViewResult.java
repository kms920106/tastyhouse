package com.tastyhouse.webapplication.shop.port.out;

import java.util.Map;

/**
 * 가게 리뷰 통계(손님 화면) — 총 평점만 가게 쪽에서 온다.
 *
 * <p><b>챕터 10</b>에서 신설. {@code totalRating}은 리뷰 통계 투영이 아니라 <b>가게 단건</b>
 * ({@code ShopQueryPort#findVisibleDetailById})의 값이라, 이 화면 계약은 두 포트를 합쳐야 만들어진다 —
 * 그래서 공용 읽기 계약 {@code ShopReviewStatisticsResult}에 형제로 둘 수 없다(선례:
 * {@link ShopDetailViewResult}). 가게 조회가 폐업·노출정지를 차단하는 판정도 함께 서비스에 남는다.
 *
 * <p>나머지 필드는 {@code ShopReviewStatisticsResult}의 값을 그대로 옮긴 것이다.
 */
public record ShopReviewStatisticsViewResult(
    Double totalRating,
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating,
    Double averageAtmosphereRating,
    Double averageKindnessRating,
    Double averageHygieneRating,
    Double willRevisitPercentage,
    Map<Integer, Long> monthlyReviewCounts,
    Map<Integer, Long> ratingCounts
) {
}
