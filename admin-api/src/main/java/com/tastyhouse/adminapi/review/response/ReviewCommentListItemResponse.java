package com.tastyhouse.adminapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 댓글 항목 응답")
public record ReviewCommentListItemResponse(
    @Schema(description = "댓글 ID", example = "1")
    Long id,

    @Schema(description = "작성 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "작성 회원 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "댓글 내용", example = "좋은 리뷰 감사합니다")
    String content,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden,

    @Schema(description = "작성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "답글 목록")
    List<ReviewReplyListItemResponse> replies
) {
    public static ReviewCommentListItemResponse from(
        Long id,
        Long memberId,
        String memberNickname,
        String content,
        boolean hidden,
        LocalDateTime createdAt,
        List<ReviewReplyListItemResponse> replies
    ) {
        return new ReviewCommentListItemResponse(
            id,
            memberId,
            memberNickname,
            content,
            hidden,
            createdAt,
            replies
        );
    }
}
