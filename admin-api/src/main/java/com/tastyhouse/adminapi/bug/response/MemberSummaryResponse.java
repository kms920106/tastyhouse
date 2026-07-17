package com.tastyhouse.adminapi.bug.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 요약 정보")
public record MemberSummaryResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "닉네임", example = "맛집헌터")
    String nickname
) {

    public static MemberSummaryResponse from(
        Long id,
        String nickname
    ) {
        return new MemberSummaryResponse(id, nickname);
    }
}
