package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 상표 이미지 변경 요청 command.
 *
 * <p>이미지 파일은 Command 필드가 아니라 서비스 메서드의 별도 {@code MultipartFile} 파라미터로
 * 전달되므로, 텍스트 파트가 없는 이 연산은 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopTrademarkChangeRequestCommand(
    Long ceoId,
    Long shopId
) {
    public ShopTrademarkChangeRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopTrademarkChangeRequestCommand of(Long ceoId, Long shopId) {
        return new ShopTrademarkChangeRequestCommand(ceoId, shopId);
    }
}
