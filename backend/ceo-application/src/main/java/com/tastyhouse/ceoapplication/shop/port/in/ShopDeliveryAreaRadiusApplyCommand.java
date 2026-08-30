package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 반경 기반 배달가능지역 적용 command.
 *
 * <p>{@code replace}가 {@code true}면 기존 지역을 교체하고, {@code false}면 병합한다 — 기존 의미 그대로다.
 */
public record ShopDeliveryAreaRadiusApplyCommand(
    Long ceoId,
    Long shopId,
    Integer radiusMeters,
    Boolean replace
) {
    public ShopDeliveryAreaRadiusApplyCommand {
        if (ceoId == null || shopId == null || radiusMeters == null || replace == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
