package com.tastyhouse.adminapi.rank.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 랭킹 목록 항목 응답")
public record RankMemberListItemResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "닉네임", example = "맛집헌터")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.tastyhouse.com/profile/1.jpg")
    String profileImageUrl,

    @Schema(description = "리뷰 작성 수", example = "42")
    Integer reviewCount,

    @Schema(description = "랭킹 순위", example = "1")
    Integer rankNo,

    @Schema(description = "회원 등급", example = "GOLD")
    String grade
) {

    public static RankMemberListItemResponse of(
        Long memberId,
        String nickname,
        String profileImageUrl,
        Integer reviewCount,
        Integer rankNo,
        String grade
    ) {
        return new RankMemberListItemResponse(
            memberId,
            nickname,
            profileImageUrl,
            reviewCount,
            rankNo,
            grade
        );
    }
}
