package com.tastyhouse.application.review.port.out;

/**
 * 월별 리뷰 통계 한 칸 — 연월(yyyy-MM)·월 평균 평점·월 리뷰 수.
 *
 * <p><b>챕터 09</b>에서 신설. 최근 6개월을 <b>정확히 6칸</b>으로 채우는 작업(DAO는 리뷰가 있는 달만
 * 돌려주므로 빈 달을 메운다)이 여러 조회 결과를 합치는 application의 일이므로, 그 결과를 나른다.
 */
public record ShopReviewMonthlyStatResult(
    String yearMonth,
    Double averageRating,
    Long reviewCount
) {
}
