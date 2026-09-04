package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 옵션 삭제 command.
 */
public record ProductOptionDeleteCommand(
    Long ceoId,
    Long optionId,
    Long shopId
) {
    public ProductOptionDeleteCommand {
        if (ceoId == null
            || optionId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
