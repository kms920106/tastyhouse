package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand}로 주입한다.
 */
public record ProductUpdateCommand(
    Long ceoId,
    Long productId,
    Long shopId,
    Long productCategoryId,
    String name,
    String composition,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    Boolean singleServing,
    Integer spiciness,
    Boolean representative,
    Boolean ratingExcluded,
    String weightText
) {
    public ProductUpdateCommand {
        if (ceoId == null
            || productId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
