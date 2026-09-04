package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션 품절·숨김 해제 command. {@code target}은 경계 타입인 문자열이고 enum 승격은 서비스가 한다.
 */
public record ProductOptionReleaseCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options,
    String target
) {
    public ProductOptionReleaseCommand {
        if (ceoId == null
            || shopId == null
            || options == null
            || target == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
