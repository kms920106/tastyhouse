package com.tastyhouse.core.entity.order;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId; // 주문 상품 ID (ORDER_ITEM.id 참조)

    @Column(name = "option_group_id")
    private Long optionGroupId; // 옵션 그룹 ID (PRODUCT_OPTION_GROUP.id 참조, null이면 삭제된 옵션 그룹)

    @Column(name = "option_group_name", nullable = false, length = 100)
    private String optionGroupName; // 주문 당시 옵션 그룹명 (스냅샷)

    @Column(name = "option_id")
    private Long optionId; // 옵션 ID (PRODUCT_OPTION.id 참조, null이면 삭제된 옵션)

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName; // 주문 당시 옵션명 (스냅샷)

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice; // 옵션 추가 금액

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
