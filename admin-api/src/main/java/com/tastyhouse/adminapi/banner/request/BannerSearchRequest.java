package com.tastyhouse.adminapi.banner.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 검색 요청")
public record BannerSearchRequest(
    @Schema(description = "배너 유형 (미지정 시 전체 유형 조회)", example = "HOME", allowableValues = {"HOME", "SIDEBAR"})
    String type,

    @Schema(description = "제목 (부분 일치 검색)", example = "여름 프로모션")
    String title,

    @Schema(description = "노출 여부 (null=전체/true=노출/false=비노출)", example = "true")
    Boolean visible
) {
}
