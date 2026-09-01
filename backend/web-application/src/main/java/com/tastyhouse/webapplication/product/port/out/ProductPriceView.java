package com.tastyhouse.webapplication.product.port.out;

/**
 * 메뉴 가격 한 행 — 주문유형으로 <b>이미 해석된</b> 단일 가격.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ProductPriceResult}는 채널별 가격 세 벌
 * ({@code deliveryPrice}·{@code storePrice}·{@code pickupPrice})을 그대로 나르므로 이 응답을 표현할 수
 * 없다 — 어느 채널 가격을 쓸지 고르는 것은 <b>도메인 모델의 계산</b>
 * ({@code ProductPrice#resolvePrice})이고, api 모듈은 도메인 모델을 알 수 없다
 * ({@code apiModuleShouldBeDomainModelFree}). 그래서 해석은 서비스에서 끝내고 그 결과만 이 계약으로
 * 나른다.
 *
 * <p>세 벌을 그대로 내려주지 않는 이유는 {@code ProductPriceResponse}의 Javadoc에 있다 — 화면이
 * 배달가·픽업가 중에서 고르게 하면 클라이언트가 픽업가를 주장해 배달을 싸게 사는 우회가 생긴다.
 * 매장가는 결제에 쓰이지 않는 표시 전용 값이라 손님 계약에 담지 않는다.
 *
 * <p>컴포넌트 이름과 순서는 {@code ProductPriceResponse}를 그대로 승계한다 — 응답 JSON의 필드 구성이
 * 불변이어야 하고, 순서가 어긋나면 컴파일은 통과한 채 값만 조용히 뒤바뀐다.
 */
public record ProductPriceView(
    Long priceId,
    String priceName,
    Integer price
) {
}
