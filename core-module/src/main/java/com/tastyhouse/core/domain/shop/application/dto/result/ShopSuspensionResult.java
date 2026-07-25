package com.tastyhouse.core.domain.shop.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopSuspension;
import com.tastyhouse.core.domain.shop.domain.model.SuspensionReason;

public record ShopSuspensionResult(
    Long id,
    Long shopId,
    SuspensionReason reason,
    OrderMethod orderMethod,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime releasedAt
) {

    public static ShopSuspensionResult from(ShopSuspension shopSuspension) {
        return new ShopSuspensionResult(
            shopSuspension.getId(),
            shopSuspension.getShopId(),
            shopSuspension.getReason(),
            shopSuspension.getOrderMethod(),
            shopSuspension.getStartAt(),
            shopSuspension.getEndAt(),
            shopSuspension.getReleasedAt()
        );
    }
}
