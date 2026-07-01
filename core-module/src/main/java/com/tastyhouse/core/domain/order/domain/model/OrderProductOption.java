package com.tastyhouse.core.domain.order.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ORDER_PRODUCT_OPTION")
public class OrderProductOption extends BaseEntity {

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

    private OrderProductOption(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice
    ) {
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice != null ? additionalPrice : 0;
    }

    public static OrderProductOption of(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice
    ) {
        return new OrderProductOption(
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice
        );
    }
}
