package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 콘텐츠보드 등록 command.
 *
 * <p>이미지/GIF 파일은 Command 필드가 아니라 서비스 메서드의 별도 {@code MultipartFile} 파라미터로
 * 전달된다. {@code youtubeUrl}·{@code description}은 콘텐츠 형태에 따라 비는 값이라 가드를 걸지 않는다.
 */
public record ShopContentBoardCreateCommand(
    Long ceoId,
    Long shopId,
    String contentType,
    String topic,
    String youtubeUrl,
    String description
) {
    public ShopContentBoardCreateCommand {
        if (ceoId == null || shopId == null || contentType == null || topic == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
