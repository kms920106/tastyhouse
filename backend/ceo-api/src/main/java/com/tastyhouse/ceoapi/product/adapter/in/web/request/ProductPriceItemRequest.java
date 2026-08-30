package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapplication.product.port.in.ProductPriceItemCommand;

/**
 * 메뉴 가격 행 하나.
 *
 * <p>{@code id}가 있으면 기존 행 갱신, 비어 있으면 신규 추가다 — 전체 교체(PUT)라 요청에 담기지 않은
 * 기존 행은 삭제되므로, 화면은 살릴 행의 {@code id}를 반드시 실어 보내야 한다.
 *
 * <p><b>{@code storePrice}·{@code pickupPrice}에 {@code @NotNull}을 붙이지 않는다.</b> 두 값은 매장
 * 가격 인증을 받은 가게만 채울 수 있고 미인증 가게는 비워 보내는 것이 정상 요청이다. 인증 게이트는
 * 도메인({@code ProductPriceService})이 {@code PRODUCT_PRICE_STORE_NOT_VERIFIED}로 판정한다.
 *
 * <p>가격명도 {@code @NotBlank}가 아니다 — 가격 행이 1개면 가격명이 없어도 되고, 2개 이상일 때만
 * 필수라는 <b>집합 제약</b>이라 필드 하나로 판정할 수 없다(도메인이
 * {@code PRODUCT_PRICE_NAME_REQUIRED}로 판정).
 */
@Schema(description = "메뉴 가격 행")
public record ProductPriceItemRequest(
    @Schema(description = "가격 행 ID. 신규 추가면 비웁니다", example = "10")
    Long id,

    @Size(max = 50, message = "가격명은 50자 이하여야 합니다.")
    @Schema(description = "가격명(가격 행이 2개 이상이면 필수)", example = "대")
    String priceName,

    @NotNull(message = "배달가격은 필수입니다.")
    @PositiveOrZero(message = "배달가격은 0원 이상이어야 합니다.")
    @Schema(description = "배달가격(원)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer deliveryPrice,

    @PositiveOrZero(message = "매장가격은 0원 이상이어야 합니다.")
    @Schema(description = "매장가격(원). 매장 가격 인증을 받은 가게만 설정할 수 있습니다", example = "14000")
    Integer storePrice,

    @PositiveOrZero(message = "픽업가격은 0원 이상이어야 합니다.")
    @Schema(description = "픽업가격(원). 매장 가격 인증을 받은 가게만 설정할 수 있습니다", example = "14000")
    Integer pickupPrice,

    @NotNull(message = "표시 순서는 필수입니다.")
    @PositiveOrZero(message = "표시 순서는 0 이상이어야 합니다.")
    @Schema(description = "표시 순서(0부터). sort=0 행의 배달가가 메뉴 대표가로 동기화됩니다", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort
) {

    /**
     * 같은 타입의 금액 필드가 연달아 있어 위치 기반 조립은 뒤바뀜을 컴파일러가 잡지 못한다.
     * 반드시 이름 기반 접근자로 조립한다.
     */
    public ProductPriceItemCommand toCommand() {
        return new ProductPriceItemCommand(
            this.id(),
            this.priceName(),
            this.deliveryPrice(),
            this.storePrice(),
            this.pickupPrice(),
            this.sort()
        );
    }
}
