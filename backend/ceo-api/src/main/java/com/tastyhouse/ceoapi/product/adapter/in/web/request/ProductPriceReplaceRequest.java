package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductPriceReplaceCommand;

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
