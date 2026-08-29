package com.tastyhouse.ceoapi.product.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 병합 제안 제외 command.
 */
public record ProductOptionGroupMergeExclusionCreateCommand(
    Long ceoId,
    Long shopId,
    String signature,
    List<Long> optionGroupIds
) {
    public ProductOptionGroupMergeExclusionCreateCommand {
        if (ceoId == null
            || shopId == null
            || signature == null
            || optionGroupIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
