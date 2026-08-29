package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 변경이력 대분류 카탈로그 항목 응답. 자기 하위 중분류 목록을 함께 담아 필터 드롭다운 2단을 한 번에
 * 채운다.
 */
@Schema(description = "변경이력 대분류")
public record ShopChangeCategoryResponse(

    @Schema(description = "대분류 코드", example = "OPERATION")
    String code,

    @Schema(description = "대분류 한글 라벨", example = "운영 정보")
    String name,

    @Schema(description = "이 대분류에 속한 중분류 목록")
    List<ShopChangeTypeResponse> changeTypes
) {

    public static ShopChangeCategoryResponse from(
        String code,
        String name,
        List<ShopChangeTypeResponse> changeTypes
    ) {
        return new ShopChangeCategoryResponse(
            code,
            name,
            changeTypes
        );
    }
}
