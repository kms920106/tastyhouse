package com.tastyhouse.application.review.port.out;

/**
 * 항목별 평점 평균 묶음.
 *
 * <p>전부 {@code Double}이라 위치를 바꿔도 컴파일된다 — 이 record를 조립·소비할 때 컴포넌트 선언 순서와
 * 인자 순서를 하나씩 대조한다(맛→양→가격→분위기→친절→위생).
 *
 * <p>리뷰가 0건이면 각 필드가 {@code null}이다.
 */
public record ShopReviewCategoryAverageResult(
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating
) {
}
