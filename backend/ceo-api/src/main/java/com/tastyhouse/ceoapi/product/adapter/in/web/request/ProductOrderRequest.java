package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.product.port.in.ProductReorderCommand;

/**
 * 그룹 내 메뉴 순서 변경 요청(replace-all).
 *
 * <p>{@code productCategoryId}에 {@code @NotNull}을 붙이지 않는다 — 미분류({@code null}) 메뉴 목록도
 * 정당한 재정렬 대상이다.
 */
@Schema(description = "그룹 내 메뉴 순서 변경 요청")
public record ProductOrderRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Schema(description = "대상 메뉴그룹 ID. 지정하지 않으면 미분류 메뉴 목록이 대상이다.", example = "10")
    Long productCategoryId,

    @NotEmpty(message = "메뉴 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 그 그룹의 메뉴 ID 전체 목록", example = "[5, 2, 9]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds
) {

    public ProductReorderCommand toCommand(Long ceoId) {
        return new ProductReorderCommand(ceoId, shopId, productCategoryId, productIds);
    }
}
