package com.tastyhouse.domain.order.model;

import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

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
    private final Integer additionalPrice; // 옵션 추가 금액 (보증금은 여기 포함되지 않는다)
    /**
     * 주문 시점 옵션그룹 유형 스냅샷({@code NORMAL} / {@code CUP_DEPOSIT}).
     *
     * <p>유형 <b>이름</b>을 박제하는 이유는 옵션그룹 행의 현재 유형을 되짚으면 안 되기 때문이다 —
     * 유형 전환 경로를 두지 않았으므로 지금은 바뀌지 않지만, 이 주문이 무엇이었는지는 주문 행 자신이
     * 답할 수 있어야 한다.
     */
    private final String optionGroupType;
    /** 주문 시점 일회용컵 제공 개수 스냅샷. 환급 단위가 컵 개수라 금액만으로는 대체할 수 없다. */
    private final Integer cupCount;
    /**
     * 주문 시점 보증금 금액 스냅샷(= {@code cupCount} × 당시 요율).
     *
     * <p>{@code additionalPrice}와 <b>별도 항목</b>이다 — 합치면 비과세 신고·정산이 사후에 보증금을
     * 분리할 방법이 영구히 사라진다(요율이 그 후 바뀌었을 수 있어 {@code optionId}로 되짚는 것은
     * 답이 아니다).
     */
    private final Integer depositAmount;

    private OrderProductOption(
        Long id,
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        this.id = id;
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.optionGroupType = optionGroupType;
        this.cupCount = cupCount;
        this.depositAmount = depositAmount != null ? depositAmount : 0;
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
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOption(
            null,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice != null ? additionalPrice : 0,
            optionGroupType,
            cupCount,
            depositAmount
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
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOption(
            id,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice,
            optionGroupType,
            cupCount,
            depositAmount
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

    public String getOptionGroupType() {
        return this.optionGroupType;
    }

    public Integer getCupCount() {
        return this.cupCount;
    }

    /** 주문 시점 보증금 금액. 비과세·정산 제외 항목이며 {@code additionalPrice}에 포함되지 않는다. */
    public Integer getDepositAmount() {
        return this.depositAmount;
    }
}
