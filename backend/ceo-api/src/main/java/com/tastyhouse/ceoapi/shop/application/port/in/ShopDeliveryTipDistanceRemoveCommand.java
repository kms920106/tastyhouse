package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 거리별 추가 배달팁 해제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopDeliveryTipDistanceRemoveCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryTipDistanceRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipDistanceRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipDistanceRemoveCommand(ceoId, shopId);
    }
}
