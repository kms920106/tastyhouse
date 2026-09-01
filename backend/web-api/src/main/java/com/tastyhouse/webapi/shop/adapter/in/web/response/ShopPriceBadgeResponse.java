package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.shop.port.out.ShopPriceBadgeViewResult;

/**
 * 가게 매장가격 뱃지 2종의 노출 여부.
 *
 * <p><b>플래그만 내려주고 판정 근거(매장가·픽업가·커버리지 비율)는 담지 않는다.</b> 근거를 함께
 * 내리면 손님 앱이 자체 판정을 시도할 수 있고, 매장가는 결제에 쓰이지 않는 표시 전용 값이라 손님
 * 계약에 노출할 것이 아니다. 화면은 이 두 boolean으로 뱃지를 켜고 끄기만 한다.
 *
 * <p>두 뱃지는 <b>서로 독립</b>이다 — 조건이 달라 한쪽만 켜지는 경우가 정상이다
 * ({@code StorePriceBadgePolicy}).
 */
@Schema(description = "가게 매장가격 뱃지 노출 여부")
public record ShopPriceBadgeResponse(
    @Schema(description = "'매장과 같은 가격' 뱃지 노출 여부. 가게의 매장가격 인증이 켜져 있으면 true입니다.",
        example = "true")
    boolean sameAsStorePrice,

    @Schema(description = "'매장가격 픽업' 뱃지 노출 여부. 픽업가가 매장가 이하이고 전체 메뉴의 80% 이상이 "
        + "매장가·픽업가를 가지며, 픽업가 설정 익일(영업일)이 지났을 때 true입니다.",
        example = "false")
    boolean storePricePickup
) {
    public static ShopPriceBadgeResponse from(ShopPriceBadgeViewResult result) {
        return new ShopPriceBadgeResponse(result.sameAsStorePrice(), result.storePricePickup());
    }
}
