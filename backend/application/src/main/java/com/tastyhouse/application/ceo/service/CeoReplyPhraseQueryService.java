package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoReplyPhraseQueryService implements CeoReplyPhraseQueryUseCase {

    private final CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort;

    public CeoReplyPhraseQueryService(CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort) {
        this.ceoReplyPhraseQueryPort = ceoReplyPhraseQueryPort;
    }

    @Override
    public List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId) {
        return ceoReplyPhraseQueryPort.findReplyPhrases(ceoId);
    }
}
