package com.tastyhouse.webapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;

@Schema(description = "답글 등록 요청")
public record ReplyCreateRequest(
    @Schema(description = "답글 대상 멤버 ID (다른 답글에 대한 답글인 경우)", example = "2")
    Long replyToMemberId,

    @NotBlank(message = "답글 내용은 필수입니다")
    @Schema(description = "답글 내용", example = "저도 그렇게 생각해요!")
    String content
) {
    public ReviewReplyCreateCommand toCommand(Long memberId, Long commentId) {
        return new ReviewReplyCreateCommand(
            memberId,
            commentId,
            replyToMemberId,
            content
        );
    }
}
