package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 삭제 command.
 */
public record ProductOptionGroupDeleteCommand(
    Long ceoId,
    Long optionGroupId,
    Long shopId
) {
    public ProductOptionGroupDeleteCommand {
        if (ceoId == null
            || optionGroupId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
