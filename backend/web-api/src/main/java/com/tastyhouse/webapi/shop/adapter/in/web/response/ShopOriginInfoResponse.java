package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

@Schema(description = "가게 원산지 표시 정보")
public record ShopOriginInfoResponse(
    @Schema(description = "입력 방식", example = "DIRECT", allowableValues = {"DIRECT", "FRANCHISE_URL"})
    String sourceType,

    @Schema(description = "직접 입력 본문. sourceType=FRANCHISE_URL이면 null",
        example = "돼지고기: 국내산, 쇠고기: 미국산")
    String content,

    @Schema(description = "본사 제공 URL. sourceType=DIRECT이면 null", example = "https://example.com/origin")
    String url
) {
    public static ShopOriginInfoResponse from(ShopOriginInfoResult result) {
        return new ShopOriginInfoResponse(
            result.sourceType(),
            result.content(),
            result.url()
        );
    }
}
