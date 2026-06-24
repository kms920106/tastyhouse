package com.tastyhouse.webapi.follow.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record IsFollowingResponse(
    @Schema(description = "조회 대상 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "내가 해당 회원을 팔로우 중인지 여부", example = "true")
    boolean following
) {
    public static IsFollowingResponse of(Long memberId, boolean following) {
        return new IsFollowingResponse(memberId, following);
    }
}
