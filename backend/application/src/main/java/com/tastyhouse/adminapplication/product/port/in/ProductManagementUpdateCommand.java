package com.tastyhouse.adminapplication.product.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 관리자 상품 수정 command. {@code productId}는 경로 변수라 컨트롤러가 주입한다. */
public record ProductManagementUpdateCommand(
    Long productId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Boolean representative,
    Integer spiciness,
    Boolean soldOut,
    Boolean visible,
    Integer sort
) {
    public ProductManagementUpdateCommand {
        if (productId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
