package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 가격 한 행(가격명 + 주문유형으로 <b>이미 해석된</b> 단일 가격).
 *
 * <p><b>채널별 가격 세 벌을 내려주지 않는다.</b> 어느 가격을 쓸지는 서버가 주문유형으로 단독
 * 결정한다({@code ProductPrice#resolvePrice}) — 화면이 배달가·픽업가 중에서 고르게 하면 클라이언트가
 * 픽업가를 주장해 배달을 싸게 사는 우회가 생기고, 주문 금액 검증과 표시 가격이 갈린다.
 *
 * <p><b>매장가({@code storePrice})는 이 응답에 없다.</b> 매장가는 결제에 쓰이지 않는 표시 전용 값이며
 * 그 쓰임은 가게 단위 뱃지({@code GET /api/shops/v1/&#123;id&#125;/price-badges})뿐이다. 메뉴마다 매장가를
 * 함께 내리면 이 계약에 없는 오프라인 가격표가 손님 앱으로 새어 나간다.
 */
@Schema(description = "메뉴 가격 (주문유형으로 해석된 단일 가격)")
public record ProductPriceResponse(
    @Schema(description = "가격 ID. 주문 생성 시 이 값을 함께 보내야 서버가 같은 가격 행으로 금액을 검증합니다.",
        example = "1")
    Long priceId,

    @Schema(description = "가격명(보통/곱빼기 등). 가격 행이 하나뿐인 단일 가격 메뉴는 null입니다.",
        example = "곱빼기")
    String priceName,

    @Schema(description = "요청한 주문유형에 적용되는 결제 가격. 포장(TAKEOUT)이면 픽업가(미설정 시 배달가), "
        + "그 외에는 배달가입니다.", example = "16650")
    Integer price
) {
    public static ProductPriceResponse from(Long priceId, String priceName, Integer price) {
        return new ProductPriceResponse(priceId, priceName, price);
    }
}
