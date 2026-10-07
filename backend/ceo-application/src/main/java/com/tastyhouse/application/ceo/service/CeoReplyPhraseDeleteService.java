package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseDeleteCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseDeleteUseCase;

@Service
@Transactional
class CeoReplyPhraseDeleteService implements CeoReplyPhraseDeleteUseCase {

    private final CeoReplyPhraseService ceoReplyPhraseService;

    public CeoReplyPhraseDeleteService(CeoReplyPhraseService ceoReplyPhraseService) {
        this.ceoReplyPhraseService = ceoReplyPhraseService;
    }

    @Override
    public void remove(CeoReplyPhraseDeleteCommand command) {
        ceoReplyPhraseService.remove(command.ceoId(), command.replyPhraseId());
    }
}
