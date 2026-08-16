package com.tastyhouse.webapi.menureview.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품별 메뉴 평가 목록 항목")
public record MenuReviewListItemResponse(
    @Schema(description = "메뉴 평가 ID", example = "77")
    Long id,

    @Schema(description = "작성자 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL. 없으면 null입니다.")
    String memberProfileImageUrl,

    @Schema(description = "메뉴 평점 (1~5)", example = "5")
    Integer rating,

    @Schema(description = "짧은 코멘트. 미입력이면 null입니다.", example = "양념이 딱 좋았어요")
    String comment,

    @Schema(description = "작성일시", example = "2026-06-19T20:11:00")
    LocalDateTime createdAt
) {

    public static MenuReviewListItemResponse from(
        Long id,
        String memberNickname,
        String memberProfileImageUrl,
        Integer rating,
        String comment,
        LocalDateTime createdAt
    ) {
        return new MenuReviewListItemResponse(
            id,
            memberNickname,
            memberProfileImageUrl,
            rating,
            comment,
            createdAt
        );
    }
}
