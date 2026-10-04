package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
