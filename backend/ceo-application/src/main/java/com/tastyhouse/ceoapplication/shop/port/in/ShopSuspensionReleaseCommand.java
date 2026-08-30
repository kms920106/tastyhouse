package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 영업 임시중지 해제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopSuspensionReleaseCommand(
    Long ceoId,
    Long shopId,
    Long suspensionId
) {
    public ShopSuspensionReleaseCommand {
        if (ceoId == null || shopId == null || suspensionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopSuspensionReleaseCommand of(Long ceoId, Long shopId, Long suspensionId) {
        return new ShopSuspensionReleaseCommand(ceoId, shopId, suspensionId);
    }
}
