package com.tastyhouse.application.ceo.port.out.write;

import java.util.List;
import java.util.Optional;

public interface CeoReplyPhraseStatePort {
    Optional<CeoReplyPhraseState> findById(Long id);

    List<CeoReplyPhraseState> findAllByCeoId(Long ceoId);

    long countByCeoId(Long ceoId);

    CeoReplyPhraseState save(CeoReplyPhraseState state);

    void delete(Long id);
}
