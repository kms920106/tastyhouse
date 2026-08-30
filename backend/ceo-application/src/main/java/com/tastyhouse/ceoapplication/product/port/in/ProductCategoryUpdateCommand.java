package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand}로 주입한다.
 */
public record ProductCategoryUpdateCommand(
    Long ceoId,
    Long productCategoryId,
    Long shopId,
    String name,
    String description
) {
    public ProductCategoryUpdateCommand {
        if (ceoId == null
            || productCategoryId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
