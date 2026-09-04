package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달팁 지역별 한 줄 command.
 *
 * <p>과거 서비스가 {@code ShopDeliveryTipRegionItemRequest}를 그대로 받던 자리를 대체한다(챕터 02 §5).
 */
public record ShopDeliveryTipRegionCommand(
    Long adminDongId,
    Integer tipAmount
) {
    public ShopDeliveryTipRegionCommand {
        if (adminDongId == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionCommand of(Long adminDongId, Integer tipAmount) {
        return new ShopDeliveryTipRegionCommand(adminDongId, tipAmount);
    }
}
