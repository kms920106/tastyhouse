package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductShopLinkReplaceCommand;

@Schema(description = "메뉴-가게 연결 전체 교체 요청")
public record ProductShopLinkReplaceRequest(

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "요청 주체 가게 ID(소유권 검증 기준)", example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "메뉴는 최소 1개 가게에 연결되어야 합니다.")
    @Valid
    @Schema(description = "연결할 가게 목록(전체 교체 — 담기지 않은 가게는 연결 해제됩니다)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductShopLinkItemRequest> links
) {
    public ProductShopLinkReplaceCommand toCommand(Long ceoId, Long productId) {
        return new ProductShopLinkReplaceCommand(
            ceoId,
            this.shopId(),
            productId,
            this.links() == null ? null : this.links().stream().map(ProductShopLinkItemRequest::toCommand).toList()
        );
    }
}
