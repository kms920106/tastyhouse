package com.tastyhouse.ceoapi.region.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행정동 검색 결과 항목")
public record AdminDongItemResponse(
    @Schema(description = "행정동 ID. 배달가능지역 등록 시 adminDongId로 넘긴다", example = "1")
    Long id,

    @Schema(description = "행정동 코드(10자리)", example = "1168051000")
    String code,

    @Schema(description = "표시용 전체 지역명(서버가 조립)", example = "서울특별시 강남구 역삼1동")
    String regionName
) {

    public static AdminDongItemResponse from(
        Long id,
        String code,
        String regionName
    ) {
        return new AdminDongItemResponse(
            id,
            code,
            regionName
        );
    }
}
