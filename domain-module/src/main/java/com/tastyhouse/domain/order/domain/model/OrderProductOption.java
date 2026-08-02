package com.tastyhouse.domain.order.domain.model;

import com.tastyhouse.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.domain.vo.ProductOptionId;

/**
 * 주문 상품 옵션 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code OrderProductOptionJpaEntity} + {@code OrderProductOptionMapper}가 담당한다.
 */
public class OrderProductOption {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final OrderProductId orderProductId; // 주문 상품 ID (ORDER_PRODUCT.id 참조)
    private final ProductOptionGroupId optionGroupId; // 옵션 그룹 ID (스냅샷, NULL 가능)
    private final String optionGroupName; // 주문 시점 옵션 그룹 이름 (스냅샷)
    private final ProductOptionId optionId; // 옵션 ID (스냅샷, NULL 가능)
    private final String optionName; // 주문 시점 옵션 이름 (스냅샷)
    private final Integer additionalPrice; // 옵션 추가 금액

    private OrderProductOption(
        Long id,
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice
    ) {
        this.id = id;
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
    }

    /**
     * 신규 주문 상품 옵션을 생성한다. 아직 영속되지 않았으므로 식별자가 없다.
     */
    public static OrderProductOption of(
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice
    ) {
        return new OrderProductOption(
            null,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice != null ? additionalPrice : 0
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static OrderProductOption reconstitute(
        Long id,
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice
    ) {
        return new OrderProductOption(
            id,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice
        );
    }

    public Long getId() {
        return this.id;
    }

    public OrderProductId getOrderProductId() {
        return this.orderProductId;
    }

    public ProductOptionGroupId getOptionGroupId() {
        return this.optionGroupId;
    }

    public String getOptionGroupName() {
        return this.optionGroupName;
    }

    public ProductOptionId getOptionId() {
        return this.optionId;
    }

    public String getOptionName() {
        return this.optionName;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }
}
