package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

@Schema(description = "평점별 매장 리뷰 응답")
public record ShopReviewsByRatingResponse(
    @Schema(description = "평점(1~5)별 리뷰 목록 맵")
    Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating,
    @Schema(description = "전체 리뷰 목록")
    List<ShopReviewListItemResponse> allReviews,
    @Schema(description = "전체 리뷰 개수", example = "42")
    Long totalReviewCount
) {
    /**
     * 평점별 묶음은 {@code ReviewsByRatingResult}가 이미 만든 것이다 — 평점을 키로 나누고 각 묶음의
     * 개수를 제한하는 규칙은 리뷰 조회 유스케이스가 소유하며, 여기서는 각 항목을 응답으로 옮기기만 한다.
     */
    public static ShopReviewsByRatingResponse from(ReviewsByRatingResult result) {
        return new ShopReviewsByRatingResponse(
            result.reviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(ShopReviewListItemResponse::from)
                        .toList()
                )),
            result.allReviews().stream().map(ShopReviewListItemResponse::from).toList(),
            result.totalReviewCount()
        );
    }
}
