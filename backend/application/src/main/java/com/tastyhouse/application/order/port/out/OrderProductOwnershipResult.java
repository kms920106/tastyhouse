package com.tastyhouse.application.order.port.out;

/**
 * 주문 상품 한 건의 소유·상품 식별 정보.
 *
 * <p>리뷰 작성 화면이 "이 주문 상품이 내 것인가"를 판정하고 대상 상품을 찾기 위해 필요한 최소 필드만
 * 담는다. 과거에는 이 조회가 write 포트({@code OrderProductRepository}·{@code OrderRepository})로
 * 애그리거트 둘을 로드해 필드를 꺼내는 형태였고, 그 탓에 {@code ReviewQueryService}가 write 포트를 들고
 * 있어야 해 CQRS 교차 주입 금지 규칙의 예외로 남아 있었다.
 *
 * <p>{@code orderMethod}는 화면에 그대로 표시되는 표현용이라 도메인 enum이 아니라 이름 문자열로 담는다
 * (미설정이면 {@code null}).
 *
 * <p>{@code orderMemberId}가 {@code null}이면 <b>주문 상품은 있는데 그 주문이 없다</b>는 뜻이다
 * ({@code ORDER_PRODUCT.order_id}에 FK 제약이 없어 가능한 상태). 소비 측은 이 경우를
 * {@code ORDER_NOT_FOUND}로 구분해야 한다 — 애그리거트 둘을 차례로 로드하던 이전 형태가 내던 것과
 * 같은 에러 코드를 유지하기 위함이다.
 *
 * <p>여기서 하는 판정은 <b>도메인 불변식 검증이 아니라 화면 접근 제어</b>다 — 주문 상태를 바꾸지 않고
 * 식별자만 대조하므로 표현 목적 조회로 내리는 것이 맞다. 상태를 변경하는 경로의 소유권 검증은 그대로
 * write 포트가 담당한다.
 */
public record OrderProductOwnershipResult(
    Long orderId,
    Long orderMemberId,
    Long productId,
    String orderMethod
) {
}
