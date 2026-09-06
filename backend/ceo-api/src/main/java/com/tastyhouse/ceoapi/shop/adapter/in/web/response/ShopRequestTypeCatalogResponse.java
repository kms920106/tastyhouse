package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;

@Schema(description = "요청처리 현황 필터 카탈로그")
public record ShopRequestTypeCatalogResponse(

    @Schema(description = "요청 유형 목록")
    List<ShopRequestTypeResponse> requestTypes,

    @Schema(description = "처리 상태 목록")
    List<ShopRequestStatusResponse> statuses
) {
    public static ShopRequestTypeCatalogResponse from(ShopRequestTypeCatalogResult result) {
        return new ShopRequestTypeCatalogResponse(
            result.requestTypes().stream()
                .map(ShopRequestTypeResponse::from)
                .toList(),
            result.statuses().stream()
                .map(ShopRequestStatusResponse::from)
                .toList()
        );
    }
}
