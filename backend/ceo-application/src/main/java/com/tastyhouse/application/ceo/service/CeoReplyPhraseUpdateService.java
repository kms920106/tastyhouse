package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateUseCase;

@Service
@Transactional
class CeoReplyPhraseUpdateService implements CeoReplyPhraseUpdateUseCase {

    private final CeoReplyPhraseService ceoReplyPhraseService;

    public CeoReplyPhraseUpdateService(CeoReplyPhraseService ceoReplyPhraseService) {
        this.ceoReplyPhraseService = ceoReplyPhraseService;
    }

    @Override
    public void modify(CeoReplyPhraseUpdateCommand command) {
        ceoReplyPhraseService.modify(
            command.ceoId(),
            command.replyPhraseId(),
            command.name(),
            command.content()
        );
    }
}
