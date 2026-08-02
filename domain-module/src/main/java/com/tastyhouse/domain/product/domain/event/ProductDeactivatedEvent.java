package com.tastyhouse.domain.product.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

public record ProductDeactivatedEvent(
    ProductId productId,
    ShopId shopId,
    LocalDateTime occurredAt
) {
}
