package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

/**
 * 메뉴 가격 한 행의 read model(채널별 가격 세 벌 + 픽업가 설정 시각).
 *
 * <p>같은 데이터를 도메인 서비스도 읽지만(가격 교체·인증 재판정), 그쪽은 도메인 모델
 * {@code ProductPrice}를 write 포트 {@code ProductPriceRepository}로 로드한다 — 목적(불변식 vs 표현)과
 * 반환 타입이 달라 중복이 아니다({@code ShopBusinessHourResult}가 같은 판단을 따른다).
 *
 * <p><b>손님에게 그대로 내려가는 형태가 아니다.</b> {@code storePrice}는 '매장과 같은 가격'·'매장가격
 * 픽업' 뱃지의 <b>판정 근거일 뿐 표시 전용</b>이므로, 손님 응답({@code ProductPriceResponse})에는
 * 주문유형으로 해석된 단일 가격만 담고 이 필드는 담지 않는다. 뱃지 판정이 매장가·픽업가·설정 시각을
 * 모두 필요로 해서 read model에는 남긴다.
 */
public record ProductPriceResult(
    Long id,
    Long productId,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort,
    LocalDateTime pickupPriceSetAt
) {
}
