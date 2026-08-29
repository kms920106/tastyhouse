package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 공지 등록 command.
 *
 * <p>첨부 이미지는 Command 필드가 아니라 서비스 메서드의 별도 {@code List<MultipartFile>} 파라미터로
 * 전달된다. {@code exposed}는 미지정을 허용하므로(등록만 하고 노출하지 않는 경로) 가드를 걸지 않는다.
 */
public record ShopNoticeCreateCommand(
    Long ceoId,
    Long shopId,
    String content,
    Boolean exposed
) {
    public ShopNoticeCreateCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
