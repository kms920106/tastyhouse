package com.tastyhouse.webapi.review.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewCommentListView;

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
    public static ReviewCommentResponse from(ReviewCommentListView.CommentWithReplies item) {
        return new ReviewCommentResponse(
            item.comment().id(),
            item.comment().reviewId(),
            item.comment().memberId(),
            item.comment().memberNickname(),
            item.comment().memberProfileImageUrl(),
            item.comment().content(),
            item.comment().createdAt(),
            item.replies().stream().map(ReviewReplyResponse::from).toList()
        );
    }
}
