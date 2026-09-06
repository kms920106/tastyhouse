package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CeoReplyPhraseDeleteCommand(
    Long ceoId,
    Long replyPhraseId
) {
    public CeoReplyPhraseDeleteCommand {
        if (ceoId == null || replyPhraseId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoReplyPhraseDeleteCommand of(Long ceoId, Long replyPhraseId) {
        return new CeoReplyPhraseDeleteCommand(ceoId, replyPhraseId);
    }
}
