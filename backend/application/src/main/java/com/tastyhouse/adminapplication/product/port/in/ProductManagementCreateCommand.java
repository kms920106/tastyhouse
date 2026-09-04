package com.tastyhouse.adminapplication.product.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 관리자 상품 등록 command.
 *
 * <p>형식·필수 검증은 Request의 jakarta.validation이 담당하고, 이 record는 구조적 가드만 둔다.
 */
public record ProductManagementCreateCommand(
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    Boolean representative,
    Integer spiciness,
    Boolean soldOut,
    Boolean visible,
    Integer sort
) {
    public ProductManagementCreateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
