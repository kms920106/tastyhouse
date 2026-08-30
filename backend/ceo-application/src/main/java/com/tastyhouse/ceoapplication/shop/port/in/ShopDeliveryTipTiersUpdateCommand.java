package com.tastyhouse.ceoapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 구간별 기본 배달팁 일괄 교체 command(replace-all 의미 그대로).
 */
public record ShopDeliveryTipTiersUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipTierCommand> tiers
) {
    public ShopDeliveryTipTiersUpdateCommand {
        if (ceoId == null || shopId == null || tiers == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
