package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "플레이스 리뷰 응답")
public record PlaceReviewResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "회원명 (닉네임)", example = "먹는게제일좋아")
    String memberNickname,

    @Schema(description = "리뷰 내용", example = "샌드위치 종류는 햄치즈...")
    String content,

    @Schema(description = "총 평점", example = "3.5")
    Double totalRating,

    @Schema(description = "맛 평점", example = "3.8")
    Double tasteRating,

    @Schema(description = "양 평점", example = "3.6")
    Double amountRating,

    @Schema(description = "가격 평점", example = "3.9")
    Double priceRating,

    @Schema(description = "분위기 평점", example = "3.8")
    Double atmosphereRating,

    @Schema(description = "친절 평점", example = "3.6")
    Double kindnessRating,

    @Schema(description = "위생 평점", example = "3.9")
    Double hygieneRating,

    @Schema(description = "재방문 의사", example = "true")
    Boolean willRevisit,

    @Schema(description = "리뷰 이미지 목록")
    List<ReviewImageItem> images,

    @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt
) {
    @Schema(description = "리뷰 이미지 정보")
    public record ReviewImageItem(
        @Schema(description = "이미지 ID", example = "1")
        Long id,

        @Schema(description = "이미지 URL", example = "https://example.com/review-image.jpg")
        String imageUrl,

        @Schema(description = "정렬 순서", example = "1")
        Integer sort
    ) {
    }
}
