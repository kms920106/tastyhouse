package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

@Schema(description = "평점별 상품 리뷰 응답")
public record ProductReviewsByRatingResponse(
    @Schema(description = "평점(1~5)별 리뷰 목록")
    Map<Integer, List<ProductReviewListItemResponse>> reviewsByRating,
    @Schema(description = "전체 리뷰 목록")
    List<ProductReviewListItemResponse> allReviews,
    @Schema(description = "전체 리뷰 개수", example = "42")
    Long totalReviewCount
) {
    public static ProductReviewsByRatingResponse from(ReviewsByRatingResult result) {
        return new ProductReviewsByRatingResponse(
            result.reviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(ProductReviewListItemResponse::from)
                        .toList()
                )),
            result.allReviews().stream().map(ProductReviewListItemResponse::from).toList(),
            result.totalReviewCount()
        );
    }
}
