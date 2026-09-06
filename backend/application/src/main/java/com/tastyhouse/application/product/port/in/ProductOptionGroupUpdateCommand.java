package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupUpdateCommand(
    Long ceoId,
    Long optionGroupId,
    Long shopId,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect
) {
    public ProductOptionGroupUpdateCommand {
        if (ceoId == null
            || optionGroupId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
