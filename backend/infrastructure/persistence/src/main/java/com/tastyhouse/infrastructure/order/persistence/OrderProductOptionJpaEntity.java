package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "ORDER_PRODUCT_OPTION")
public class OrderProductOptionJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId;

    @Column(name = "option_group_id")
    private Long optionGroupId;

    @Column(name = "option_group_name", nullable = false, length = 100)
    private String optionGroupName;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    @Column(name = "option_group_type", nullable = false, length = 20)
    private String optionGroupType;

    @Column(name = "cup_count")
    private Integer cupCount;

    @Column(name = "deposit_amount", nullable = false)
    private Integer depositAmount;

    protected OrderProductOptionJpaEntity() {
    }

    private OrderProductOptionJpaEntity(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.optionGroupType = optionGroupType;
        this.cupCount = cupCount;
        this.depositAmount = depositAmount;
    }

    static OrderProductOptionJpaEntity create(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOptionJpaEntity(
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

    public Long getOrderProductId() {
        return this.orderProductId;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }
}
