package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand}로 주입한다.
 */
public record ProductOptionGroupUpdateCommand(
    Long ceoId,
    Long optionGroupId,
    Long shopId,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect
) {
    public ProductOptionGroupUpdateCommand {
        if (ceoId == null
            || optionGroupId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
