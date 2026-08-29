package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 노출 상태 응답")
public record ShopStatusResponse(
    @Schema(description = "노출정지 여부", example = "false")
    boolean hidden,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed
) {
    public static ShopStatusResponse from(boolean hidden, boolean permanentlyClosed) {
        return new ShopStatusResponse(hidden, permanentlyClosed);
    }
}
