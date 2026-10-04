package com.tastyhouse.application.ceo.port.in;

public interface CeoReplyPhraseCommandUseCase {

    Long register(CeoReplyPhraseCreateCommand command);

    void modify(CeoReplyPhraseUpdateCommand command);

    void remove(CeoReplyPhraseDeleteCommand command);
}
