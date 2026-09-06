package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface CeoReplyPhraseCommandUseCase {

    Long register(CeoReplyPhraseCreateCommand command);

    void modify(CeoReplyPhraseUpdateCommand command);

    void remove(CeoReplyPhraseDeleteCommand command);
}
