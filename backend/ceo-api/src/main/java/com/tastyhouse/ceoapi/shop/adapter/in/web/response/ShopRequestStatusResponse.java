package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;

/**
 * 요청 상태 카탈로그 항목. 필터 드롭다운을 채우는 데 쓴다.
 */
@Schema(description = "요청 상태 카탈로그 항목")
public record ShopRequestStatusResponse(

    @Schema(description = "상태 코드", example = "PENDING")
    String code,

    @Schema(description = "상태 한글 라벨", example = "대기중")
    String description
) {

    public static ShopRequestStatusResponse from(ShopRequestStatus result) {
        return new ShopRequestStatusResponse(
            result.name(),
            result.getDescription()
        );
    }
}
