package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

@Schema(description = "가게 주문안내 응답")
public record ShopOrderNoticeResponse(
    @Schema(description = "주문안내 본문", example = "포장 주문은 매장에서 10분 정도 소요됩니다.")
    String content
) {
    public static ShopOrderNoticeResponse from(ShopOrderNoticeResult result) {
        return new ShopOrderNoticeResponse(result.content());
    }
}
