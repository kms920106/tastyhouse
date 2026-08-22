package com.tastyhouse.webapi.product.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "상품 배치 조회 요청. (상품ID, 옵션ID) 조합의 목록입니다.")
public record ProductBatchRequest(
    @Schema(description = "조회할 항목 목록")
    @NotEmpty(message = "조회할 항목 목록은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "한 번에 조회할 수 있는 항목은 최대 200개입니다.")
    @Valid
    List<BatchItemRequest> items,

    // 가격 행({@code prices})의 채널 가격을 해석할 주문유형.
    //
    // 장바구니는 손님이 고른 가격 행({@code priceId})으로 금액을 표시해야 하는데, 어느 채널 가격을
    // 쓸지는 서버가 주문유형으로 단독 결정한다({@code ProductPrice#resolvePrice}). 그래서 배치 조회도
    // 상세 조회와 같은 파라미터를 받아 이미 해석된 단일 가격만 내려준다 — 화면이 배달가/픽업가를
    // 고르면 주문 접수의 {@code validateAmounts()}와 어긋나 전 주문이 거절된다.
    //
    // 미지정이면 {@code DELIVERY}로 정규화한다(상세 조회와 동일한 기본값).
    @Schema(description = "가격을 해석할 주문유형. 미지정이면 DELIVERY로 조회합니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "TAKEOUT")
    String orderMethod
) {
    /** 주문유형 미지정 시 기본값 — 손님 화면의 기본 진입 경로가 배달이다. */
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    public ProductBatchRequest {
        if (orderMethod == null || orderMethod.isBlank()) {
            orderMethod = DEFAULT_ORDER_METHOD;
        }
    }
}
