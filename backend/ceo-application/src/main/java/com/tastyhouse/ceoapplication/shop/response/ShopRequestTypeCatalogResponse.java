package com.tastyhouse.ceoapplication.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청처리 현황 필터 카탈로그 응답(요청 유형 + 처리 상태).
 *
 * <p>한글 라벨을 서버가 내려 프론트 상수 복제를 막는다. 가게에 종속되지 않는 정적 카탈로그라 소유권 검증이
 * 없다({@code /v1/change-history-types} 선례).
 */
@Schema(description = "요청처리 현황 필터 카탈로그")
public record ShopRequestTypeCatalogResponse(

    @Schema(description = "요청 유형 목록")
    List<ShopRequestTypeResponse> requestTypes,

    @Schema(description = "처리 상태 목록")
    List<ShopRequestStatusResponse> statuses
) {

    public static ShopRequestTypeCatalogResponse from(
        List<ShopRequestTypeResponse> requestTypes,
        List<ShopRequestStatusResponse> statuses
    ) {
        return new ShopRequestTypeCatalogResponse(
            requestTypes,
            statuses
        );
    }
}
