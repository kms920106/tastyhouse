package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.SuspensionReason;

public record ShopSuspensionCreateCommand(
    Long shopId,
    SuspensionReason reason,
    OrderMethod orderMethod,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    public static ShopSuspensionCreateCommand of(
        Long shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new ShopSuspensionCreateCommand(shopId, reason, orderMethod, startAt, endAt);
    }
}
