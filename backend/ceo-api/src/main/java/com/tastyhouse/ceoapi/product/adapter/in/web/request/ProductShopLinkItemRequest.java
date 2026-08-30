package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkItemCommand;

/**
 * 메뉴를 연결할 가게 한 건(가게 + 그 가게에서의 메뉴그룹).
 *
 * <p>{@code productCategoryId}가 필수인 이유는 <b>가게마다 메뉴그룹이 다르기 때문</b>이다. 원본 가게의
 * 메뉴그룹을 그대로 쓸 수 없으므로(다른 가게에는 그 그룹이 없다) 연결할 때마다 그 가게의 그룹을 고른다.
 *
 * <p>{@code sort}(표시 순서)는 받지 않는다 — 새 연결은 대상 가게 메뉴판 끝에 붙이는 것이 서버 규칙이며,
 * 요청이 순서를 정하면 그 가게의 기존 배열을 헤집는다.
 */
@Schema(description = "메뉴-가게 연결 항목")
public record ProductShopLinkItemRequest(

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "연결할 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "메뉴가 노출될 메뉴그룹을 선택해 주세요.")
    @Schema(description = "이 가게에서 메뉴가 노출될 메뉴그룹 ID", example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Long productCategoryId
) {

    public ProductShopLinkItemCommand toCommand() {
        return new ProductShopLinkItemCommand(shopId, productCategoryId);
    }
}
