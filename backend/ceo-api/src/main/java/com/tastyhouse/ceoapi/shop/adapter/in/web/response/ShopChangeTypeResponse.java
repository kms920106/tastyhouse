package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shared.port.out.CodeLabelResult;

@Schema(description = "변경이력 중분류")
public record ShopChangeTypeResponse(

    @Schema(description = "중분류 코드", example = "BUSINESS_HOUR")
    String code,

    @Schema(description = "중분류 한글 라벨", example = "영업시간")
    String name
) {
    public static ShopChangeTypeResponse from(CodeLabelResult result) {
        return new ShopChangeTypeResponse(
            result.code(),
            result.label()
        );
    }
}
