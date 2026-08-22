package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 매장 가격 인증 대상 항목 한 건.
 *
 * <p><b>이 record는 JSON 바디가 아니라 multipart의 {@code items} 파트 문자열에서 파싱된다</b> —
 * 가격표 이미지와 대상 목록이 한 요청에 함께 와야 하므로 요청 형식이 {@code multipart/form-data}이고,
 * 그 안에서 목록만 JSON 문자열로 전달된다. 따라서 {@code @Valid} 전파가 자동으로 걸리지 않아
 * command 서비스가 파싱 후 검증기를 명시적으로 태운다.
 *
 * <p>{@code storePrice}는 <b>요청 시점의 값으로 박제</b>되어 승인 시 그대로 반영된다 — 승인이 나중에
 * 이뤄지므로 그 사이 점주가 가격을 바꿔도 검수자가 본 가격이 반영돼야 한다.
 */
@Schema(description = "매장 가격 인증 대상 항목")
public record ShopStorePriceVerificationItemRequest(
    @NotNull(message = "메뉴 ID는 필수입니다.")
    @Schema(description = "대상 메뉴 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @NotNull(message = "가격 행 ID는 필수입니다.")
    @Schema(description = "대상 가격 행 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    Long priceId,

    @NotNull(message = "매장가격은 필수입니다.")
    @PositiveOrZero(message = "매장가격은 0원 이상이어야 합니다.")
    @Schema(description = "인증받을 매장가격(원). 요청 시점 값으로 박제됩니다", example = "14000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer storePrice,

    @Schema(description = "승인 시 픽업가격도 매장가격과 같게 설정할지", example = "true")
    Boolean applyPickupSamePrice
) {
}
