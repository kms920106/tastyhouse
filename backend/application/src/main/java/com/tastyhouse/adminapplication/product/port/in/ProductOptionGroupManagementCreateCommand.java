package com.tastyhouse.adminapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 상품 옵션그룹 등록 command.
 *
 * <p>{@code groupType}은 경계에서 문자열로 받고 서비스가 {@code ProductOptionGroupType}으로 승격한다.
 */
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
