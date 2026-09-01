package com.tastyhouse.webapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

/**
 * 평점별 상품 리뷰 목록 + 전체 건수.
 *
 * <p><b>공용 {@code PaginationResponse<T>}로 대체되지 않는다</b> — 4필드 표준 페이징 래퍼가 아니라
 * 중첩 {@code response}와 {@code totalElements}만 갖는 자체 형태다. 페이징 형태를 표준으로 바꾸면
 * 응답 JSON이 달라지므로 이 계약을 그대로 유지한다.
 */
@Schema(description = "평점별 상품 리뷰 목록과 페이지네이션 정보 응답")
public record ProductReviewsByRatingPageResponse(
    @Schema(description = "평점별 상품 리뷰 응답")
    ProductReviewsByRatingResponse response,
    @Schema(description = "전체 리뷰 개수", example = "42")
    long totalElements
) {

    public static ProductReviewsByRatingPageResponse from(ReviewsByRatingResult result) {
        return new ProductReviewsByRatingPageResponse(
            ProductReviewsByRatingResponse.from(result),
            result.totalElements()
        );
    }
}
