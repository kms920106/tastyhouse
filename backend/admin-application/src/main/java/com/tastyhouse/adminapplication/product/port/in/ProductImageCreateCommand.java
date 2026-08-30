package com.tastyhouse.adminapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 상품 이미지 등록 command. {@code productId}는 경로 변수라 컨트롤러가 주입한다. */
public record ProductImageCreateCommand(
    Long productId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ProductImageCreateCommand {
        if (productId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
