package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
