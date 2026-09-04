package com.tastyhouse.webapi.review.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;

@Schema(description = "리뷰 등록/수정 응답")
public record ReviewResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "맛 평점", example = "4.0")
    Double tasteRating,

    @Schema(description = "양 평점", example = "3.0")
    Double amountRating,

    @Schema(description = "가격 평점", example = "3.0")
    Double priceRating,

    @Schema(description = "총 평점", example = "3.3")
    Double totalRating,

    @Schema(description = "리뷰 내용")
    String content,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "태그 목록")
    List<String> tags,

    @Schema(description = "작성일시")
    LocalDateTime createdAt
) {
    public static ReviewResponse from(ReviewSubmitResultView view) {
        return new ReviewResponse(
            view.reviewId(),
            view.productId(),
            view.tasteRating(),
            view.amountRating(),
            view.priceRating(),
            view.totalRating(),
            view.content(),
            view.imageUrls(),
            view.tags(),
            view.createdAt()
        );
    }
}
