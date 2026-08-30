package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 등록 command. {@code groupType}은 경계 타입인 문자열이고 enum 승격은 서비스가 한다.
 */
public record ProductOptionGroupCreateCommand(
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
    public ProductOptionGroupCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
