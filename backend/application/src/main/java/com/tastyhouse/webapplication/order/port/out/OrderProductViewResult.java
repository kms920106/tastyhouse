package com.tastyhouse.webapplication.order.port.out;

import java.util.List;

import com.tastyhouse.application.order.port.out.OrderProductOptionResult;

/**
 * 주문상품 항목 + 리뷰 작성 여부.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code OrderProductResult}에 {@code reviewed} 하나가
 * 더 붙은 형태다. 그 값은 주문 조회 포트의 투영이 아니라 <b>리뷰 컨텍스트에 물어본 결과</b>라
 * ({@code ReviewQueryService#findReviewedProductIds}) 공유 패키지에 형제로 둘 수 없다 — 포트 하나의
 * 산출물이 아니기 때문이다.
 *
 * <p><b>N+1을 피하는 조립 순서가 계약의 일부다</b>: 상품마다 리뷰 여부를 개별 조회하면 상품 수만큼
 * 쿼리가 나가므로, 서비스가 상품 식별자를 모아 한 번에 조회하고 그 집합으로 이 값을 채운다. 판정을
 * web-api로 내리면 그 배치 조회가 깨진다.
 *
 * <p>금액 필드는 {@code OrderProductResult}의 값을 그대로 옮겨 담기만 한다(계산 없음).
 */
public record OrderProductViewResult(
    Long orderProductId,
    Long productId,
    String name,
    String priceName,
    String imageUrl,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    List<OrderProductOptionResult> options,
    boolean reviewed
) {
}
