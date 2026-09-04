package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 자주 쓰는 문구 쓰기 인바운드 포트.
 */
@CeoApp
public interface CeoReplyPhraseCommandUseCase {

    Long register(CeoReplyPhraseCreateCommand command);

    void modify(CeoReplyPhraseUpdateCommand command);

    void remove(CeoReplyPhraseDeleteCommand command);
}
