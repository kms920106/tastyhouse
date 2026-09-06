package com.tastyhouse.domain.order.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 주문 상품 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code OrderProductJpaEntity} + {@code OrderProductMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code OrderProductRepository#save}를 호출해야 한다.
 */
public class OrderProduct {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final OrderId orderId; // 주문 ID (ORDERS.id 참조)
    private final ProductId productId; // 상품 ID (PRODUCT.id 참조)
    private final String name; // 주문 시점 상품명 (스냅샷)
    /**
     * 주문 시점 가격명 (스냅샷). 가격이 하나뿐인 메뉴는 {@code null}이다.
     *
     * <p>박제하는 이유는 {@code name}과 같다 — 나중에 점주가 가격명("보통"→"기본")을 바꿔도 과거 주문
     * 전표의 표기가 변하면 안 된다. 가격명은 메뉴정보·주문정보·주문전표에서 메뉴의 하위 항목으로 표시된다.
     */
    private final String priceName;
    private final UploadedFileId imageFileId; // 주문 시점 상품 이미지 파일 ID (스냅샷). 이미지 없으면 null
    private final Integer quantity; // 수량
    private final Integer originalPrice; // 정가
    private final Integer discountPrice; // 할인가
    private Integer totalOptionPrice; // 옵션 금액 합계
    private Integer totalPrice; // 상품 총 금액
    /**
     * 이 라인의 일회용컵 보증금 합계(수량 반영).
     *
     * <p><b>{@code totalOptionPrice}·{@code totalPrice}에는 포함되지 않는다.</b> 포함하면 그 값이
     * 주문 전체의 {@code totalProductAmount}로 흘러들어 최소주문금액·쿠폰·포인트 기준액까지 오염된다
     * ({@code Order.cupDepositAmount} 주석 참조).
     */
    private Integer cupDepositAmount;

    private OrderProduct(
        Long id,
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.priceName = priceName;
        this.imageFileId = imageFileId;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount != null ? cupDepositAmount : 0;
    }

    /**
     * 신규 주문 상품을 생성한다. 아직 영속되지 않았으므로 식별자가 없다.
     */
    public static OrderProduct of(
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        return new OrderProduct(
            null,
            orderId,
            productId,
            name,
            priceName,
            imageFileId,
            quantity != null ? quantity : 1,
            originalPrice != null ? originalPrice : 0,
            discountPrice,
            totalOptionPrice != null ? totalOptionPrice : 0,
            totalPrice != null ? totalPrice : 0,
            cupDepositAmount != null ? cupDepositAmount : 0
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static OrderProduct reconstitute(
        Long id,
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        return new OrderProduct(
            id,
            orderId,
            productId,
            name,
            priceName,
            imageFileId,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            cupDepositAmount
        );
    }

    /**
     * 확정된 라인 금액을 반영한다.
     *
     * <p>{@code cupDepositAmount}를 함께 받되 {@code totalPrice}와 <b>분리해서</b> 저장한다 —
     * 필드 주석대로 보증금이 상품 금액에 섞이면 최소주문금액·쿠폰·포인트 기준액이 오염된다.
     */
    public void updatePrices(Integer totalOptionPrice, Integer totalPrice, Integer cupDepositAmount) {
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount != null ? cupDepositAmount : 0;
    }

    /** 이 라인의 보증금 합계. {@code totalPrice}에 포함되지 않는 별도 항목이다. */
    public Integer getCupDepositAmount() {
        return this.cupDepositAmount;
    }

    public Long getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    /** 주문 시점 가격명(스냅샷). 가격이 하나뿐인 메뉴는 {@code null}이다. */
    public String getPriceName() {
        return this.priceName;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public Integer getOriginalPrice() {
        return this.originalPrice;
    }

    public Integer getDiscountPrice() {
        return this.discountPrice;
    }

    public Integer getTotalOptionPrice() {
        return this.totalOptionPrice;
    }

    public Integer getTotalPrice() {
        return this.totalPrice;
    }

    public OrderProductId getOrderProductId() {
        return OrderProductId.of(this.id);
    }
}
