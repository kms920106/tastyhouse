package com.tastyhouse.domain.ceo.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;

public interface CeoReplyPhraseRepository {
    Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId);

    List<CeoReplyPhrase> findAllByCeoId(CeoId ceoId);

    long countByCeoId(CeoId ceoId);

    CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase);

    void delete(CeoReplyPhrase ceoReplyPhrase);
}
