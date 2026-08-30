package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 픽업 위치 초기화 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopRiderPickupLocationClearCommand(
    Long ceoId,
    Long shopId
) {
    public ShopRiderPickupLocationClearCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRiderPickupLocationClearCommand of(Long ceoId, Long shopId) {
        return new ShopRiderPickupLocationClearCommand(ceoId, shopId);
    }
}
