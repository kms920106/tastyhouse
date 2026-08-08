package com.tastyhouse.infrastructure.order.query;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 주문 상품 라인 조회 결과 — 주문 시점 스냅샷(상품명·이미지 파일·수량·가격)과 선택 옵션 목록을 담는다.
 *
 * <p>{@code imageUrl}은 주문 시점에 스냅샷한 {@code ORDER_PRODUCT.image_file_id}
 * (={@code UPLOADED_FILE.id})를 join해 얻은 저장 경로를, 다른 도메인과 동일하게
 * {@code FileUrlResolver}로 표시용 URL까지 변환한 값이다 — DAO가 fetch 직후
 * {@link #withResolvedImageUrl}로 재조립한다. 이 record에 담기는 시점에는 이미 URL이므로 필드명이
 * 값과 일치한다.
 *
 * <p>파일 <b>ID</b>를 스냅샷하는 이유: 이후 상품 대표 이미지가 교체되거나 비노출 처리돼도 과거 주문은
 * 주문 당시 파일을 계속 가리켜야 한다(이력 보존). {@code UPLOADED_FILE} 행은 불변이라 이 참조는
 * 안정적이다. 반대로 경로 문자열을 스냅샷하던 과거 설계는 컬럼명({@code image_url})과 실제 저장값(경로)이
 * 어긋나 조회 경로가 resolver를 건너뛰는 장애를 낳았다.
 *
 * <p>{@code options}는 QueryDSL 한 번의 투영으로 채울 수 없어(1:N), {@code @QueryProjection}은 옵션을
 * 제외한 좁은 생성자에 붙이고 DAO가 별도 조회한 옵션을 {@link #withResolvedImageUrl}로 덧붙인다(review 도메인의
 * {@code ReviewDetailResult} 선례와 동일한 관용구).
 */
public record OrderProductResult(
    Long orderProductId,
    Long productId,
    String name,
    String imageUrl,
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
        String imageUrl,
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
            imageUrl,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            List.of()
        );
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꾸고 별도 조회한 옵션을 함께 덧붙인다. {@code @QueryProjection}이
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 DAO가 fetch 직후 호출한다.
     */
    public OrderProductResult withResolvedImageUrl(String resolvedImageUrl, List<OrderProductOptionResult> options) {
        return new OrderProductResult(
            orderProductId,
            productId,
            name,
            resolvedImageUrl,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            options
        );
    }
}
