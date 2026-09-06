package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    String groupType
) {
    public ProductOptionGroupOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
