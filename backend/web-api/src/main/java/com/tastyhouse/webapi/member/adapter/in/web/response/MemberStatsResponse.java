package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.member.port.out.MemberStatsResult;

@Schema(description = "회원 통계 응답")
public record MemberStatsResponse(
    @Schema(description = "리뷰 수", example = "7")
    long reviewCount,

    @Schema(description = "팔로잉 수", example = "12")
    long followingCount,

    @Schema(description = "팔로워 수", example = "34")
    long followerCount
) {
    public static MemberStatsResponse from(MemberStatsResult result) {
        return new MemberStatsResponse(
            result.reviewCount(),
            result.followingCount(),
            result.followerCount()
        );
    }
}
