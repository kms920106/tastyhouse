package com.tastyhouse.application.product.port.out;

/**
 * 상품 리뷰 통계 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ProductReviewStatisticsResult}로는 이 응답을
 * 표현할 수 없다 — <b>{@code totalRating}의 출처가 리뷰 통계가 아니라 상품 투영</b>
 * ({@code ProductDetailResult#rating})이다. 두 출처를 합치는 판단이라 서비스에 남는다. 표현 계층이
 * 두 계약을 받아 어느 필드를 어디서 꺼내는지 고르게 하면 그 판단이 조용히 복제된다.
 *
 * <p>리뷰가 하나도 없으면 평균 3필드는 모두 {@code null}이고 {@code totalReviewCount}만 0이다 —
 * 0.0으로 뭉개지 않는다. 평점 0점은 정당한 값이라 미집계와 합치면 화면이 "0점"을 표시한다.
 *
 * <p>컴포넌트 이름과 순서는 {@code ProductReviewStatisticsResponse}를 그대로 승계한다.
 */
public record ProductReviewStatisticsView(
    Double totalRating,
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating
) {
}
