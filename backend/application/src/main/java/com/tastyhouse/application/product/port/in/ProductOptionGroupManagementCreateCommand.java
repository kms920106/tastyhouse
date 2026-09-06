package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupManagementCreateCommand(
    Long productId,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    Boolean visible,
    String groupType
) {
    public ProductOptionGroupManagementCreateCommand {
        if (productId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
