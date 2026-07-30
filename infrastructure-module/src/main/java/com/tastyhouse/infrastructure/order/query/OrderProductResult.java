package com.tastyhouse.infrastructure.order.query;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 주문 상품 라인 조회 결과 — 주문 시점 스냅샷(상품명·이미지 경로·수량·가격)과 선택 옵션 목록을 담는다.
 *
 * <p>{@code options}는 QueryDSL 한 번의 투영으로 채울 수 없어(1:N), {@code @QueryProjection}은 옵션을
 * 제외한 좁은 생성자에 붙이고 DAO가 별도 조회한 옵션을 {@link #withOptions}로 덧붙인다(review 도메인의
 * {@code ReviewDetailResult} 선례와 동일한 관용구).
 */
public record OrderProductResult(
    Long orderProductId,
    Long productId,
    String name,
    String imageFilePath,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    List<OrderProductOptionResult> options
) {
    @QueryProjection
    public OrderProductResult(
        Long orderProductId,
        Long productId,
        String name,
        String imageFilePath,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice
    ) {
        this(
            orderProductId,
            productId,
            name,
            imageFilePath,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            List.of()
        );
    }

    public OrderProductResult withOptions(List<OrderProductOptionResult> options) {
        return new OrderProductResult(
            orderProductId,
            productId,
            name,
            imageFilePath,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            options
        );
    }
}
