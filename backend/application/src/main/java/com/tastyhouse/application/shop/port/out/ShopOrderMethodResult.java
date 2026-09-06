package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.OrderMethod;

public record ShopOrderMethodResult(
    Long id,
    OrderMethod orderMethod
) {
}
