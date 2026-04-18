package com.tastyhouse.webapi.review.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "답글 응답")
public record ReplyResponse(
    @Schema(description = "답글 ID", example = "1")
    Long id,

    @Schema(description = "댓글 ID", example = "1")
    Long commentId,

    @Schema(description = "작성자 ID", example = "1")
    Long memberId,

    @Schema(description = "작성자 닉네임", example = "맛집헌터")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL")
    String memberProfileImageUrl,

    @Schema(description = "답글 대상 멤버 ID", example = "2")
    Long replyToMemberId,

    @Schema(description = "답글 대상 멤버 닉네임", example = "맛집러버")
    String replyToMemberNickname,

    @Schema(description = "답글 내용", example = "저도 그렇게 생각해요!")
    String content,

    @Schema(description = "작성일시")
    LocalDateTime createdAt
) {
    public static ReplyResponse from(
    Long id,
    Long commentId,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    Long replyToMemberId,
    String replyToMemberNickname,
    String content,
    LocalDateTime createdAt
    ) {
    return new ReplyResponse(
        id,
        commentId,
        memberId,
        memberNickname,
        memberProfileImageUrl,
        replyToMemberId,
        replyToMemberNickname,
        content,
        createdAt
    );
    }
}
