package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.product.port.in.ProductPriceReplaceCommand;

/**
 * 메뉴 가격 전체 교체 요청.
 *
 * <p>{@code shopId}를 바디로 함께 받아 소유권을 검증한다 — 경로에 가게 식별자가 없으면 검증을 생략하기
 * 쉽고, 이 저장소는 그 형태로 IDOR을 낸 전례가 있다({@code ProductShopScopeRequest}와 같은 판단).
 *
 * <p>{@code prices}에 {@code @NotEmpty}를 두는 것은 도메인의 {@code PRODUCT_PRICE_EMPTY}와 <b>이중</b>
 * 방어다. 가격 0개는 어떤 해석으로도 정상 요청이 아니어서(메뉴에 가격이 없으면 주문 자체가 불가능하다)
 * 배열 개수 불변식과 달리 앞단에서 걸러도 응답 계약이 갈리지 않는다.
 */
@Schema(description = "메뉴 가격 전체 교체 요청")
public record ProductPriceReplaceRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "가격은 1개 이상 등록해야 합니다.")
    @Valid
    @Schema(description = "가격 목록(전체 교체 — 담기지 않은 기존 행은 삭제됩니다)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductPriceItemRequest> prices
) {

    public ProductPriceReplaceCommand toCommand(Long ceoId, Long productId) {
        return new ProductPriceReplaceCommand(
            ceoId,
            this.shopId(),
            productId,
            this.prices() == null ? null : this.prices().stream().map(ProductPriceItemRequest::toCommand).toList()
        );
    }
}
