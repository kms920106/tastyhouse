package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

public record ShopOrderMethodAssignCommand(
    OrderMethod orderMethod
) {

    public static ShopOrderMethodAssignCommand of(OrderMethod orderMethod) {
        return new ShopOrderMethodAssignCommand(orderMethod);
    }
}
