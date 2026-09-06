package com.tastyhouse.webapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductPriceView;

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
    public static ProductPriceResponse from(ProductPriceView view) {
        return new ProductPriceResponse(view.priceId(), view.priceName(), view.price());
    }
}
