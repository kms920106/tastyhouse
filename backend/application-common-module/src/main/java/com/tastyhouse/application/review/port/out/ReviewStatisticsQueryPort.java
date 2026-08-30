package com.tastyhouse.application.review.port.out;

import java.util.Map;

/**
 * 리뷰 통계 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>가게·상품 상세와 마이페이지에 노출할 리뷰 수와 항목별 평점 평균을 조회한다. 점주 통계 화면
 * 조회는 {@code ShopReviewStatisticsQueryPort}가 소유한다.
 *
 * <p>분할 전 한 인터페이스 안에서 {@code getRatingCounts}·{@code getMonthlyReviewCounts}는 오버로드였다.
 * 회원 화면은 기간 인자가 없는(가게 전체 / 특정 연도) 형태를, 점주 화면은 조회 기간을 받는 형태를 쓰므로
 * 시그니처가 서로 달라 분할로 각자의 포트에 하나씩 남는다 — <b>공유 메서드는 0개다.</b>
 */
public interface ReviewStatisticsQueryPort {

    Long countVisibleByShopId(Long shopId);

    Long countWillRevisit(Long shopId);

    Double getAverageTasteRating(Long shopId);

    Double getAverageAmountRating(Long shopId);

    Double getAveragePriceRating(Long shopId);

    Double getAverageAtmosphereRating(Long shopId);

    Double getAverageKindnessRating(Long shopId);

    Double getAverageHygieneRating(Long shopId);

    Map<Integer, Long> getRatingCounts(Long shopId);

    Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year);

    Long countVisibleByProductId(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    long countVisibleReviewsByMemberId(Long memberId);
}
