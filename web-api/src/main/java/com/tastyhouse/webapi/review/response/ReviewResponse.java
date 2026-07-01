package com.tastyhouse.webapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "리뷰 이미지 파일 ID 목록")
    List<Long> uploadedFileIds,

    @Schema(description = "태그 목록")
    List<String> tags,

    @Schema(description = "작성일시")
    LocalDateTime createdAt
) {
    public static ReviewResponse from(
        Long reviewId,
        Long productId,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double totalRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags,
        LocalDateTime createdAt
    ) {
        return new ReviewResponse(
            reviewId,
            productId,
            tasteRating,
            amountRating,
            priceRating,
            totalRating,
            content,
            uploadedFileIds,
            tags,
            createdAt
        );
    }
}
