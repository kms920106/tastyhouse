package com.tastyhouse.infrastructure.shop.query;

/**
 * 가게 한 곳의 배달팁 하한/상한(표현용).
 *
 * <p>목록·카드·상세·팝업이 모두 이 한 쌍을 쓰며, 산출 규칙은
 * {@link ShopDeliveryTipQueryDao#findTipRanges} 하나가 소유한다 — 규칙이 두 벌이 되면 같은 가게의
 * 목록 카드와 상세 화면이 다른 금액을 보여준다.
 */
public record ShopDeliveryTipRangeResult(Long shopId, int minDeliveryTip, int maxDeliveryTip) {

    /** 배달팁을 설정하지 않은 가게 — 하한·상한 모두 0이다. */
    public static ShopDeliveryTipRangeResult none(Long shopId) {
        return new ShopDeliveryTipRangeResult(shopId, 0, 0);
    }
}
