package com.tastyhouse.ceoapi.product.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 노출기간 교체 command. 요일·시간대는 중첩 command 목록으로 받는다.
 */
public record ProductExposureReplaceCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    LocalDate startDate,
    LocalDate endDate,
    List<ProductExposureHourCommand> hours
) {
    public ProductExposureReplaceCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
