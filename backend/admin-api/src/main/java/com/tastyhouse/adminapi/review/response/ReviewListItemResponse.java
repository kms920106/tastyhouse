package com.tastyhouse.adminapi.review.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 목록 항목 응답")
public record ReviewListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "작성 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "작성 회원 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "총 평점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "정말 맛있어요")
    String content,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden,

    @Schema(description = "사장님만보기 여부. 작성자가 비공개로 등록한 리뷰이며 hidden(게시중단)과는 독립입니다.", example = "false")
    boolean ownerOnly,

    @Schema(description = "작성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static ReviewListItemResponse from(
        Long id,
        Long shopId,
        Long productId,
        Long memberId,
        String memberNickname,
        Double totalRating,
        String content,
        boolean hidden,
        boolean ownerOnly,
        LocalDateTime createdAt
    ) {
        return new ReviewListItemResponse(
            id,
            shopId,
            productId,
            memberId,
            memberNickname,
            totalRating,
            content,
            hidden,
            ownerOnly,
            createdAt
        );
    }
}
