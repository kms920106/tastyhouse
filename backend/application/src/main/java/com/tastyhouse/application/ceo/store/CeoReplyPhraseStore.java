package com.tastyhouse.application.ceo.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseStatePort;
import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;

public class CeoReplyPhraseStore implements CeoReplyPhraseRepository {
    private final CeoReplyPhraseStatePort ceoReplyPhraseStatePort;

    public CeoReplyPhraseStore(CeoReplyPhraseStatePort ceoReplyPhraseStatePort) {
        this.ceoReplyPhraseStatePort = ceoReplyPhraseStatePort;
    }

    @Override
    public Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId) {
        return ceoReplyPhraseStatePort.findById(ceoReplyPhraseId.value()).map(CeoReplyPhraseStateMapper::toDomain);
    }

    @Override
    public List<CeoReplyPhrase> findAllByCeoId(CeoId ceoId) {
        return ceoReplyPhraseStatePort.findAllByCeoId(ceoId.value()).stream()
            .map(CeoReplyPhraseStateMapper::toDomain)
            .toList();
    }

    @Override
    public long countByCeoId(CeoId ceoId) {
        return ceoReplyPhraseStatePort.countByCeoId(ceoId.value());
    }

    @Override
    public CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase) {
        return CeoReplyPhraseStateMapper.toDomain(
            ceoReplyPhraseStatePort.save(CeoReplyPhraseStateMapper.toState(ceoReplyPhrase)));
    }

    @Override
    public void delete(CeoReplyPhrase ceoReplyPhrase) {
        ceoReplyPhraseStatePort.delete(ceoReplyPhrase.getId());
    }
}
