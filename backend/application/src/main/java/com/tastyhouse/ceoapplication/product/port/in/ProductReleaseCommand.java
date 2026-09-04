package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 품절·숨김 해제 command. {@code target}은 경계 타입인 문자열이고 enum 승격은 서비스가 한다.
 */
public record ProductReleaseCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds,
    String target
) {
    public ProductReleaseCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null
            || target == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
