package com.tastyhouse.webapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.webapplication.review.port.in.ReviewReplyCreateCommand;

@Schema(description = "답글 등록 요청")
public record ReplyCreateRequest(
    @Schema(description = "답글 대상 멤버 ID (다른 답글에 대한 답글인 경우)", example = "2")
    Long replyToMemberId,

    @NotBlank(message = "답글 내용은 필수입니다")
    @Schema(description = "답글 내용", example = "저도 그렇게 생각해요!")
    String content
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code commentId}를 주입받아 command로 변환한다.
     *
     * <p>{@code memberId}·{@code commentId}·{@code replyToMemberId} 세 {@code Long}이 연달아 있어
     * 위치 기반 전달은 작성자와 답글 대상을 조용히 뒤바꾼다 — 아래는 이름 기반 접근자로 짚어 넘긴다.
     */
    public ReviewReplyCreateCommand toCommand(Long memberId, Long commentId) {
        return new ReviewReplyCreateCommand(
            memberId,
            commentId,
            replyToMemberId,
            content
        );
    }
}
