package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 병합 command. {@code entryType}은 경계 타입인 문자열로 받고 enum 승격은 서비스가 한다.
 */
public record ProductOptionGroupMergeCommand(
    Long ceoId,
    Long shopId,
    Long baseOptionGroupId,
    List<Long> optionGroupIds,
    String entryType
) {
    public ProductOptionGroupMergeCommand {
        if (ceoId == null
            || shopId == null
            || baseOptionGroupId == null
            || optionGroupIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
