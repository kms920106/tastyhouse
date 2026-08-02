package com.tastyhouse.webapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 상세 조회 응답")
public record ReviewDetailResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "역 이름", example = "강남역")
    String stationName,

    @Schema(description = "리뷰 내용", example = "음식이 정말 맛있었어요")
    String content,

    @Schema(description = "총점", example = "4.5")
    Double totalRating,

    @Schema(description = "맛 점수", example = "4.5")
    Double tasteRating,

    @Schema(description = "양 점수", example = "4.0")
    Double amountRating,

    @Schema(description = "가격 점수", example = "4.0")
    Double priceRating,

    @Schema(description = "분위기 점수", example = "4.5")
    Double atmosphereRating,

    @Schema(description = "친절도 점수", example = "5.0")
    Double kindnessRating,

    @Schema(description = "위생 점수", example = "4.5")
    Double hygieneRating,

    @Schema(description = "재방문 의사 여부", example = "true")
    boolean willRevisit,

    @Schema(description = "작성자 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "작성자 닉네임", example = "맛집헌터")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.tastyhouse.com/member/2/profile.jpg")
    String memberProfileImageUrl,

    @Schema(description = "리뷰 작성 일시", example = "2026-06-03T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "리뷰 태그명 목록")
    List<String> tagNames
) {
    public static ReviewDetailResponse from(
        Long id,
        Long shopId,
        String shopName,
        String stationName,
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
        return new ReviewDetailResponse(
            id,
            shopId,
            shopName,
            stationName,
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
