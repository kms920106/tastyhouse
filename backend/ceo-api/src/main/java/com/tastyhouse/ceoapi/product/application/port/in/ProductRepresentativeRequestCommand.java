package com.tastyhouse.ceoapi.product.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 대표메뉴 지정 요청 command.
 */
public record ProductRepresentativeRequestCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds
) {
    public ProductRepresentativeRequestCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
