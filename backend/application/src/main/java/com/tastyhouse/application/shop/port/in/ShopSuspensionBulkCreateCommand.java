package com.tastyhouse.application.shop.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopSuspensionBulkCreateCommand(
    Long ceoId,
    List<Long> shopIds,
    String reason,
    List<String> orderMethods,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public ShopSuspensionBulkCreateCommand {
        if (ceoId == null || shopIds == null || reason == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
