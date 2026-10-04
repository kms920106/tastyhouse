package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoReplyPhraseCreateCommand(
    Long ceoId,
    String name,
    String content
) {

    public CeoReplyPhraseCreateCommand {
        if (ceoId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
