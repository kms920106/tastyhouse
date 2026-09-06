package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupMergeCommand(
    Long ceoId,
    Long shopId,
    Long baseOptionGroupId,
    List<Long> optionGroupIds,
    String entryType
) {
    public ProductOptionGroupMergeCommand {
        if (ceoId == null
            || shopId == null
            || baseOptionGroupId == null
            || optionGroupIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
