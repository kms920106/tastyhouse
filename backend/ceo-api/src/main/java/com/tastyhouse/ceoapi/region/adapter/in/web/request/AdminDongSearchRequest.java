package com.tastyhouse.ceoapi.region.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행정동 검색 요청")
public record AdminDongSearchRequest(
    @Schema(description = "시/도·시군구·행정동명 부분 일치 검색어. 비우면 전체", example = "강남구 역삼")
    String keyword
) {
}
