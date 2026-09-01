package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.shop.port.out.ShopOrderMethodItemResult;

@Schema(description = "매장 주문 방식 목록 응답")
public record ShopOrderMethodResponse(
    @Schema(description = "주문 방식 목록")
    List<ShopOrderMethodItemResponse> orderMethods
) {
    public static ShopOrderMethodResponse from(List<ShopOrderMethodItemResult> orderMethods) {
        return new ShopOrderMethodResponse(
            orderMethods.stream().map(ShopOrderMethodItemResponse::from).toList()
        );
    }
}
