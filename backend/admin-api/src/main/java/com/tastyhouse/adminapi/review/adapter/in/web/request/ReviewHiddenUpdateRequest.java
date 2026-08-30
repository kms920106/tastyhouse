package com.tastyhouse.adminapi.review.adapter.in.web.request;

import com.tastyhouse.adminapplication.review.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewReplyHiddenChangeCommand;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숨김 여부 변경 요청")
public record ReviewHiddenUpdateRequest(
    @NotNull(message = "숨김 여부는 필수입니다.")
    @Schema(description = "숨김 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean hidden
) {

    public ReviewHiddenChangeCommand toReviewCommand(Long reviewId) {
        return new ReviewHiddenChangeCommand(reviewId, hidden);
    }

    public ReviewCommentHiddenChangeCommand toCommentCommand(Long commentId) {
        return new ReviewCommentHiddenChangeCommand(commentId, hidden);
    }

    public ReviewReplyHiddenChangeCommand toReplyCommand(Long replyId) {
        return new ReviewReplyHiddenChangeCommand(replyId, hidden);
    }
}
