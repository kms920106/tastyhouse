package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 방문 안내 문구 등록·수정 command. 빈 값이면 문구가 삭제된다.
 */
public record ShopRiderVisitGuideUpdateCommand(
    Long ceoId,
    Long shopId,
    String visitGuide
) {
    public ShopRiderVisitGuideUpdateCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
