package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 옵션그룹 하나가 사용 중인 메뉴 목록 — 가게 단위 벌크 조회 응답의 그룹별 항목.
 *
 * <p>옵션그룹 연결 다이얼로그가 후보 그룹마다 {@code /option-groups/{id}/products}를 개별 호출하던
 * N+1을 없애기 위해, 가게의 옵션그룹 전체에 대한 연결 메뉴를 한 번에 담아 내려준다.
 */
@Schema(description = "옵션그룹별 연결 메뉴 목록")
public record ProductOptionGroupLinkedProductsResponse(
    @Schema(description = "옵션그룹 ID", example = "10")
    Long optionGroupId,

    @Schema(description = "이 그룹을 사용하는 메뉴 목록")
    List<ProductOptionGroupLinkedProductResponse> products
) {

    public static ProductOptionGroupLinkedProductsResponse from(
        Long optionGroupId,
        List<ProductOptionGroupLinkedProductResponse> products
    ) {
        return new ProductOptionGroupLinkedProductsResponse(optionGroupId, products);
    }
}
