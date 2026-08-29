package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달지역 도형 삭제 command. 요청 본문이 없어 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopDeliveryAreaPolygonDeleteCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryAreaPolygonDeleteCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaPolygonDeleteCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaPolygonDeleteCommand(ceoId, shopId);
    }
}
