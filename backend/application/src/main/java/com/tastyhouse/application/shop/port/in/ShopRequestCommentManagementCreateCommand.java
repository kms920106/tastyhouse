package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 요청건 답변 등록 command. {@code adminId}는 작성자라 principal에서 주입한다. */
public record ShopRequestCommentManagementCreateCommand(
    Long requestId,
    Long adminId,
    String content
) {
    public ShopRequestCommentManagementCreateCommand {
        if (requestId == null || adminId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
