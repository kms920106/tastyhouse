package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoReplyPhraseUpdateCommand(
    Long ceoId,
    Long replyPhraseId,
    String name,
    String content
) {

    public CeoReplyPhraseUpdateCommand {
        if (ceoId == null || replyPhraseId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
