package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 가게 요청 취소 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long requestId
) {
    public ShopRequestCancelCommand {
        if (ceoId == null || shopId == null || requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRequestCancelCommand of(Long ceoId, Long shopId, Long requestId) {
        return new ShopRequestCancelCommand(ceoId, shopId, requestId);
    }
}
