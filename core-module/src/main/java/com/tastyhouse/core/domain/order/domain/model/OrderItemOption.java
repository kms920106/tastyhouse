package com.tastyhouse.core.domain.order.domain.model;

import com.tastyhouse.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ORDER_ITEM_OPTION")
public class OrderItemOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

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

    private OrderItemOption(
        Long orderItemId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice
    ) {
        this.orderItemId = orderItemId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice != null ? additionalPrice : 0;
    }

    public static OrderItemOption of(
        Long orderItemId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice
    ) {
        return new OrderItemOption(
            orderItemId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice
        );
    }
}
