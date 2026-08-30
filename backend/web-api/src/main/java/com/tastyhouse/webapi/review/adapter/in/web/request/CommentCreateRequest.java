package com.tastyhouse.webapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.webapplication.review.port.in.ReviewCommentCreateCommand;

@Schema(description = "댓글 등록 요청")
public record CommentCreateRequest(
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Schema(description = "댓글 내용", example = "맛있어 보이네요!")
    String content
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code reviewId}를 주입받아 command로 변환한다.
     */
    public ReviewCommentCreateCommand toCommand(Long memberId, Long reviewId) {
        return new ReviewCommentCreateCommand(
            memberId,
            reviewId,
            content
        );
    }
}
