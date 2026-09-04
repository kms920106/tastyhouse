package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 내 옵션 정렬 변경 command.
 */
public record ProductOptionOrderChangeCommand(
    Long ceoId,
    Long shopId,
    Long optionGroupId,
    List<Long> optionIds
) {
    public ProductOptionOrderChangeCommand {
        if (ceoId == null
            || shopId == null
            || optionGroupId == null
            || optionIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
