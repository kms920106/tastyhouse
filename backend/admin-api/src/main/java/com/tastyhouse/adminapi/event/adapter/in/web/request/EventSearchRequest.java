package com.tastyhouse.adminapi.event.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 검색 요청")
public record EventSearchRequest(
    @Schema(description = "이벤트명 (부분 일치 검색)", example = "신년")
    String name,

    @Schema(description = "이벤트 상태 (미지정 시 전체 상태 조회)", example = "ACTIVE", allowableValues = {"SCHEDULED", "ACTIVE", "ENDED"})
    String status
) {
}
