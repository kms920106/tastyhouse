package com.tastyhouse.application.ceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

@Service
@Transactional(readOnly = true)
class CeoReplyPhraseQueryService implements CeoReplyPhraseQueryUseCase {

    private final CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort;

    public CeoReplyPhraseQueryService(CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort) {
        this.ceoReplyPhraseQueryPort = ceoReplyPhraseQueryPort;
    }

    @Override
    public List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId) {
        return ceoReplyPhraseQueryPort.findReplyPhrases(ceoId);
    }
}
