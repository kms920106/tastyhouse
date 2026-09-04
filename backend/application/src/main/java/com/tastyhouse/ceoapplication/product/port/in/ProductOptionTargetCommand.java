package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 품절·숨김 대상 옵션 command. {@code optionType}은 경계 타입인 문자열이고 enum 승격은 서비스가 한다.
 */
public record ProductOptionTargetCommand(
    Long optionId,
    String optionType
) {
    public ProductOptionTargetCommand {
        if (optionId == null
            || optionType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
