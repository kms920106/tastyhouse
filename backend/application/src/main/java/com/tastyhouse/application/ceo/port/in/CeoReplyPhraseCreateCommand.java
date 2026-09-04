package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 자주 쓰는 문구 등록 command. {@code ceoId}는 토큰에서, 나머지는 요청 본문에서 온다.
 */
public record CeoReplyPhraseCreateCommand(
    Long ceoId,
    String name,
    String content
) {
    public CeoReplyPhraseCreateCommand {
        if (ceoId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
