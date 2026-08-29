package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 콘텐츠보드 수정 command. 교체 파일은 별도 {@code MultipartFile} 파라미터로 전달되며, 비어 있으면
 * 기존 이미지를 유지한다.
 */
public record ShopContentBoardUpdateCommand(
    Long ceoId,
    Long shopId,
    Long contentBoardId,
    String topic,
    String youtubeUrl,
    String description
) {
    public ShopContentBoardUpdateCommand {
        if (ceoId == null || shopId == null || contentBoardId == null || topic == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
