package com.tastyhouse.webapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 리뷰 목록 조회 요청")
public record ReviewSearchRequest(
    @Schema(description = "조회 타입 (ALL: 전체, FOLLOWING: 팔로잉)", example = "ALL", allowableValues = {"ALL", "FOLLOWING"})
    String type
) {

    public ReviewSearchRequest {
        if (type == null) {
            type = "ALL";
        }
    }
}
