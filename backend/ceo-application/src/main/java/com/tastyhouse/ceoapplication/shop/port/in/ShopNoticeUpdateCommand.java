package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 공지 수정 command.
 *
 * <p>{@code keepExistingImages}가 참이면 본문만 수정하고 이미지 교체를 건너뛴다. 미지정은 거짓과 같게
 * 다뤄지므로 가드를 걸지 않는다.
 */
public record ShopNoticeUpdateCommand(
    Long ceoId,
    Long shopId,
    Long noticeId,
    String content,
    Boolean keepExistingImages
) {
    public ShopNoticeUpdateCommand {
        if (ceoId == null || shopId == null || noticeId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
