package com.tastyhouse.webapplication.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 상세 정보 (상품 정보 포함)")
public record ReviewProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "아보카도 햄치즈 샌드위치")
    String productName,

    @Schema(description = "상품 이미지 URL", example = "https://example.com/product.jpg")
    String productImageUrl,

    @Schema(description = "상품 가격 (할인가가 있으면 할인가, 없으면 원가)", example = "8500")
    Integer productPrice,

    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "리뷰 내용", example = "맛있어요!")
    String content,

    @Schema(description = "총 평점", example = "3.5")
    Double totalRating,

    @Schema(description = "맛 평점", example = "4.0")
    Double tasteRating,

    @Schema(description = "양 평점", example = "3.0")
    Double amountRating,

    @Schema(description = "가격 평점", example = "3.0")
    Double priceRating,

    @Schema(description = "분위기 평점", example = "4.0")
    Double atmosphereRating,

    @Schema(description = "친절 평점", example = "4.0")
    Double kindnessRating,

    @Schema(description = "위생 평점", example = "4.0")
    Double hygieneRating,

    @Schema(description = "재방문 의사", example = "true")
    boolean willRevisit,

    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "회원 닉네임", example = "먹는게제일좋아")
    String memberNickname,

    @Schema(description = "회원 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String memberProfileImageUrl,

    @Schema(description = "작성일", example = "2020-10-27T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "태그 이름 목록", example = "[\"#샌드위치\", \"#아보카도\", \"#브런치\"]")
    List<String> tagNames
) {
    public static ReviewProductResponse from(
        Long productId,
        String productName,
        String productImageUrl,
        Integer productPrice,
        Long reviewId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        List<String> imageUrls,
        List<String> tagNames
    ) {
        return new ReviewProductResponse(
            productId,
            productName,
            productImageUrl,
            productPrice,
            reviewId,
            content,
            totalRating,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            imageUrls,
            tagNames
        );
    }
}
