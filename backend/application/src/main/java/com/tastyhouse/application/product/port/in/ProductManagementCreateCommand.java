package com.tastyhouse.application.product.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductManagementCreateCommand(
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    Boolean representative,
    Integer spiciness,
    Boolean soldOut,
    Boolean visible,
    Integer sort
) {
    public ProductManagementCreateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
