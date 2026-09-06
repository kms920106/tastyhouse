package com.tastyhouse.ceoapi.region.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행정동 계층 조회 요청")
public record AdminDongTreeRequest(
    @Schema(description = "시/도 이름. 비우면 시/도 목록을 조회", example = "서울특별시")
    String sidoName,

    @Schema(description = "시/군/구 이름. sidoName과 함께 지정하면 행정동 목록을 조회", example = "강남구")
    String sigunguName
) {
}
