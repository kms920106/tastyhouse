package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductExposureClearCommand;
import com.tastyhouse.application.product.port.in.ProductFeedbackReadCommand;
import com.tastyhouse.application.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeClearCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianClearCommand;

/**
 * 가게 소유권 검증만 필요한 조회 조건.
 *
 * <p>{@code shopId}를 경로가 아니라 query로 받는다 — 경로에 가게 식별자가 없으면 검증을 생략하기
 * 쉽고, 이 저장소는 그 형태로 IDOR을 낸 전례가 있다.
 */
@Schema(description = "가게 범위 조회 조건")
public record ProductShopScopeRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {

    public ProductFeedbackReadCommand toFeedbackReadCommand(Long ceoId) {
        return new ProductFeedbackReadCommand(ceoId, shopId);
    }

    public ProductExposureClearCommand toExposureClearCommand(Long ceoId, Long productId) {
        return new ProductExposureClearCommand(ceoId, shopId, productId);
    }

    public ProductNutritionDeleteCommand toNutritionDeleteCommand(Long ceoId, Long productId) {
        return new ProductNutritionDeleteCommand(ceoId, shopId, productId);
    }

    public ProductRepresentativeClearCommand toRepresentativeClearCommand(Long ceoId, Long productId) {
        return new ProductRepresentativeClearCommand(ceoId, shopId, productId);
    }

    public ProductVegetarianClearCommand toVegetarianClearCommand(Long ceoId, Long productId) {
        return new ProductVegetarianClearCommand(ceoId, shopId, productId);
    }

    public ProductImageDeleteCommand toImageDeleteCommand(Long ceoId, Long imageId) {
        return new ProductImageDeleteCommand(ceoId, shopId, imageId);
    }
}
