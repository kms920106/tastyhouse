package com.tastyhouse.ceoapi.ceo.application.port.in;

/**
 * 자주 쓰는 문구 쓰기 인바운드 포트.
 */
public interface CeoReplyPhraseCommandUseCase {

    Long register(CeoReplyPhraseCreateCommand command);

    void modify(CeoReplyPhraseUpdateCommand command);

    void remove(CeoReplyPhraseDeleteCommand command);
}
