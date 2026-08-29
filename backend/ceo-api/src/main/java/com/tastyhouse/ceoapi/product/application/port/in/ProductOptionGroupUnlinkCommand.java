package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴-옵션그룹 연결 해제 command.
 */
public record ProductOptionGroupUnlinkCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    Long optionGroupId
) {
    public ProductOptionGroupUnlinkCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || optionGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
