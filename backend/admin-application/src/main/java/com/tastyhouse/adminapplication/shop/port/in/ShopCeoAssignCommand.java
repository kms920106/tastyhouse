package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 담당 점주 배정 command. */
public record ShopCeoAssignCommand(
    Long adminId,
    Long shopId,
    Long ceoId
) {
    public ShopCeoAssignCommand {
        if (adminId == null || shopId == null || ceoId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
