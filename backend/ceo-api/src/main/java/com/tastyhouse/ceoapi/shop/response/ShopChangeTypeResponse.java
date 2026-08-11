package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 변경이력 중분류 카탈로그 항목 응답. 필터 드롭다운 2단계를 채운다.
 */
@Schema(description = "변경이력 중분류")
public record ShopChangeTypeResponse(

    @Schema(description = "중분류 코드", example = "BUSINESS_HOUR")
    String code,

    @Schema(description = "중분류 한글 라벨", example = "영업시간")
    String name
) {

    public static ShopChangeTypeResponse from(
        String code,
        String name
    ) {
        return new ShopChangeTypeResponse(
            code,
            name
        );
    }
}
