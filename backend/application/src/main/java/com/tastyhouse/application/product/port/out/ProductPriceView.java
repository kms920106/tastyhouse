package com.tastyhouse.application.product.port.out;

/**
 * 메뉴 채널별 가격 한 건 — 배달가·매장가·포장가.
 *
 * <p><b>챕터 09</b>에서 신설. 이 조회는 도메인 서비스({@code ProductPriceService})가 <b>애그리거트
 * {@code ProductPrice}</b>를 돌려주는데 api 모듈은 도메인 모델을 알 수 없으므로
 * ({@code apiModuleShouldBeDomainModelFree}) 값을 이 계약으로 옮겨 나른다.
 *
 * <p>컴포넌트 이름과 순서는 기존 {@code ProductPriceResponse}를 그대로 승계한다 — 응답 JSON의 필드
 * 구성이 불변이어야 하고, {@code Integer}가 연속하는 자리에서 순서가 어긋나면 컴파일은 통과한 채 값만
 * 조용히 뒤바뀐다.
 */
public record ProductPriceView(
    Long id,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {
}
