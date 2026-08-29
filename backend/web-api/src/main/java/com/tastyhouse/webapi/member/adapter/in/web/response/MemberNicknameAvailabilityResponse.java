package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복확인 응답")
public record MemberNicknameAvailabilityResponse(
    @Schema(description = "닉네임 사용 가능 여부", example = "true")
    boolean available
) {
    public static MemberNicknameAvailabilityResponse from(boolean available) {
        return new MemberNicknameAvailabilityResponse(available);
    }
}
