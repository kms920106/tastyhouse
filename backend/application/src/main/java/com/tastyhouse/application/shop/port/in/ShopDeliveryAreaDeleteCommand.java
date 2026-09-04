package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 배달가능지역 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopDeliveryAreaDeleteCommand(
    Long ceoId,
    Long deliveryAreaId
) {
    public ShopDeliveryAreaDeleteCommand {
        if (ceoId == null || deliveryAreaId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaDeleteCommand of(Long ceoId, Long deliveryAreaId) {
        return new ShopDeliveryAreaDeleteCommand(ceoId, deliveryAreaId);
    }
}
