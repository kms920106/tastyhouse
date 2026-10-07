package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateUseCase;

@Service
@Transactional
class CeoReplyPhraseCreateService implements CeoReplyPhraseCreateUseCase {

    private final CeoReplyPhraseService ceoReplyPhraseService;

    public CeoReplyPhraseCreateService(CeoReplyPhraseService ceoReplyPhraseService) {
        this.ceoReplyPhraseService = ceoReplyPhraseService;
    }

    @Override
    public Long register(CeoReplyPhraseCreateCommand command) {
        return ceoReplyPhraseService.register(command.ceoId(), command.name(), command.content());
    }
}
