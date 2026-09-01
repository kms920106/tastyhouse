package com.tastyhouse.adminapi.bug.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

@Schema(description = "회원 요약 정보")
public record MemberSummaryResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "닉네임", example = "맛집헌터")
    String nickname
) {

    /**
     * 제보자 회원이 조회되지 않으면 {@code null}을 그대로 반환한다(승격 이전 서비스 동작 보존).
     */
    public static MemberSummaryResponse from(MemberWithProfileImageResult result) {
        if (result == null) {
            return null;
        }
        return new MemberSummaryResponse(
            result.id(),
            result.nickname()
        );
    }
}
