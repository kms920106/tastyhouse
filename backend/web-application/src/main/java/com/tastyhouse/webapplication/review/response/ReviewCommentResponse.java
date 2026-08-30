package com.tastyhouse.webapplication.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 응답")
public record ReviewCommentResponse(
    @Schema(description = "댓글 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "작성자 ID", example = "1")
    Long memberId,

    @Schema(description = "작성자 닉네임", example = "맛집러버")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL")
    String memberProfileImageUrl,

    @Schema(description = "댓글 내용", example = "맛있어 보이네요!")
    String content,

    @Schema(description = "작성일시")
    LocalDateTime createdAt,

    @Schema(description = "답글 목록")
    List<ReviewReplyResponse> replies
) {
    public static ReviewCommentResponse from(
        Long id,
        Long reviewId,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        String content,
        LocalDateTime createdAt,
        List<ReviewReplyResponse> replies
    ) {
        return new ReviewCommentResponse(
            id,
            reviewId,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            content,
            createdAt,
            replies
        );
    }
}
