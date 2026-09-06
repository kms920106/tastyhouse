package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴모음컷 검수 목록 검색 조건")
public record ShopMenuCollectionImageSearchRequest(
    @Schema(description = "승인 상태. 지정하지 않으면 전체", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status
) {
}
