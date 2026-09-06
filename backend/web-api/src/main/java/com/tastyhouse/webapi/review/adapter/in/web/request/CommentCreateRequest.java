package com.tastyhouse.webapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;

@Schema(description = "댓글 등록 요청")
public record CommentCreateRequest(
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Schema(description = "댓글 내용", example = "맛있어 보이네요!")
    String content
) {
    public ReviewCommentCreateCommand toCommand(Long memberId, Long reviewId) {
        return new ReviewCommentCreateCommand(
            memberId,
            reviewId,
            content
        );
    }
}
