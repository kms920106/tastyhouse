package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 임시 휴무 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopTemporaryClosureDeleteCommand(
    Long ceoId,
    Long temporaryClosureId
) {
    public ShopTemporaryClosureDeleteCommand {
        if (ceoId == null || temporaryClosureId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopTemporaryClosureDeleteCommand of(Long ceoId, Long temporaryClosureId) {
        return new ShopTemporaryClosureDeleteCommand(ceoId, temporaryClosureId);
    }
}
