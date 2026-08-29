package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

/**
 * 회원 노출용 가게 단건 조회 결과.
 *
 * <p>폐업({@code permanentlyClosed})·노출정지({@code hidden}) 가게는 투영되지 않으므로, 결과가 없다는
 * 것이 곧 "딥링크로도 진입할 수 없다"는 뜻이다 — 기존 {@code ShopRepository#findVisibleById}와 같은
 * 가시성 조건을 유지한다.
 *
 * <p>과거에는 이 조회가 write 포트로 가게 애그리거트를 로드해 표시 필드를 꺼내는 형태였고, 그 탓에
 * {@code ShopQueryService}가 write 포트를 들고 있어야 해 CQRS 교차 주입 금지 규칙의 예외로 남아 있었다.
 * 여기 담긴 것은 전부 응답에 그대로 실리는 표현용 필드다.
 *
 * <p><b>배달팁 계산 경로는 이 투영으로 대체하지 않는다</b> — 그쪽은 도메인 서비스
 * ({@code ShopDeliveryTipCalculator})에 애그리거트와 도메인 모델을 넘겨야 하므로 표현 목적 조회가
 * 아니며, write 포트가 담당하는 것이 맞다.
 */
public record ShopVisibleDetailResult(
    Long id,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    int minOrderAmount,
    boolean scheduledOrderEnabled
) {
}
