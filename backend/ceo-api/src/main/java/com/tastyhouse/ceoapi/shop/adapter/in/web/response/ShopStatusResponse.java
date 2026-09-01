package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopStatusResult;

@Schema(description = "내 가게 노출 상태 응답")
public record ShopStatusResponse(
    @Schema(description = "노출정지 여부", example = "false")
    boolean hidden,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed
) {
    public static ShopStatusResponse from(ShopStatusResult result) {
        return new ShopStatusResponse(result.hidden(), result.permanentlyClosed());
    }
}
