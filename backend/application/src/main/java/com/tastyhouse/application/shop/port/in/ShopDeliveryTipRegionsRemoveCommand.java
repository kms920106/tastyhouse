package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 지역별 추가 배달팁 전체 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopDeliveryTipRegionsRemoveCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryTipRegionsRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionsRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipRegionsRemoveCommand(ceoId, shopId);
    }
}
