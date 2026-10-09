package com.tastyhouse.application.ceo.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;

public interface CeoReplyPhrasePersistencePort {

    Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId);

    long countByCeoId(CeoId ceoId);

    CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase);

    void delete(CeoReplyPhrase ceoReplyPhrase);
}
