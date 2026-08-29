package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapi.product.application.port.in.ProductImageReorderCommand;

/**
 * 메뉴 이미지 순서 변경 요청(replace-all).
 *
 * <p>{@code sort} 값을 받지 않고 순서 있는 id 배열만 받는다 — 클라이언트가 계산한 정렬값을 신뢰하면
 * 중복·구멍이 생기므로 서버가 {@code 0..N-1}을 부여한다. 목록이 최신 상태와 어긋나면
 * {@code PRODUCT_ORDER_TARGET_MISMATCH}(400)로 거부한다.
 */
@Schema(description = "메뉴 이미지 순서 변경 요청")
public record ProductImageSortRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "이미지 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 그 메뉴의 이미지 ID 전체 목록", example = "[3, 1, 7]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> imageIds
) {

    public ProductImageReorderCommand toCommand(Long ceoId, Long productId) {
        return new ProductImageReorderCommand(ceoId, shopId, productId, imageIds);
    }
}
