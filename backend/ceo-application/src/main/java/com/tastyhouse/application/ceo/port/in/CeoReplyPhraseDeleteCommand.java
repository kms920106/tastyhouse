package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoReplyPhraseDeleteCommand(
    Long ceoId,
    Long replyPhraseId
) {

    public CeoReplyPhraseDeleteCommand {
        if (ceoId == null || replyPhraseId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static CeoReplyPhraseDeleteCommand of(Long ceoId, Long replyPhraseId) {
        return new CeoReplyPhraseDeleteCommand(ceoId, replyPhraseId);
    }
}
