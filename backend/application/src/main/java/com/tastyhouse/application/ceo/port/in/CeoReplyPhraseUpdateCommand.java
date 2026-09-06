package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CeoReplyPhraseUpdateCommand(
    Long ceoId,
    Long replyPhraseId,
    String name,
    String content
) {
    public CeoReplyPhraseUpdateCommand {
        if (ceoId == null || replyPhraseId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
