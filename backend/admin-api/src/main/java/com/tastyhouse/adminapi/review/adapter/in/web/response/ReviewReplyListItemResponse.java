package com.tastyhouse.adminapi.review.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;

@Schema(description = "리뷰 답글 항목 응답")
public record ReviewReplyListItemResponse(
    @Schema(description = "답글 ID", example = "1")
    Long id,

    @Schema(description = "작성 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "작성 회원 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "답글 대상 회원 ID", example = "2")
    Long replyToMemberId,

    @Schema(description = "답글 대상 회원 닉네임", example = "단골손님")
    String replyToMemberNickname,

    @Schema(description = "답글 내용", example = "감사합니다!")
    String content,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden,

    @Schema(description = "작성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static ReviewReplyListItemResponse from(ReviewReplyListItemResult result) {
        return new ReviewReplyListItemResponse(
            result.id(),
            result.memberId(),
            result.memberNickname(),
            result.replyToMemberId(),
            result.replyToMemberNickname(),
            result.content(),
            result.hidden(),
            result.createdAt()
        );
    }
}
