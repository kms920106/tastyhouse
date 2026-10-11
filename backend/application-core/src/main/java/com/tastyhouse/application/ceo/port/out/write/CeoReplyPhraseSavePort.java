package com.tastyhouse.application.ceo.port.out.write;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;

public interface CeoReplyPhraseSavePort {

    CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase);

    void delete(CeoReplyPhrase ceoReplyPhrase);
}
