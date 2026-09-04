package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 이미지 변경 요청 command.
 *
 * <p>{@code MultipartFile}은 경계 타입이 아니라 command 필드로 담지 않는다 —
 * 업로드 파일은 서비스 메서드의 별도 파라미터로 전달된다.
 */
public record ProductImageChangeRequestCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductImageChangeRequestCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageChangeRequestCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductImageChangeRequestCommand(ceoId, shopId, productId);
    }
}
