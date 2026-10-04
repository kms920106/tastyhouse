package com.tastyhouse.application.shop.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopSuspensionCreateCommand(
    Long ceoId,
    Long shopId,
    String reason,
    List<String> orderMethods,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    public ShopSuspensionCreateCommand {
        if (ceoId == null || shopId == null || reason == null || startAt == null || endAt == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
