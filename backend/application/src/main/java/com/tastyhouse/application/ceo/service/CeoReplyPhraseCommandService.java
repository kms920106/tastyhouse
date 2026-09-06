package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseDeleteCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateCommand;
import com.tastyhouse.domain.ceo.service.CeoReplyPhraseService;

@Service
@CeoApp
@Transactional
public class CeoReplyPhraseCommandService implements CeoReplyPhraseCommandUseCase {

    private final CeoReplyPhraseService ceoReplyPhraseService;

    public CeoReplyPhraseCommandService(CeoReplyPhraseService ceoReplyPhraseService) {
        this.ceoReplyPhraseService = ceoReplyPhraseService;
    }

    @Override
    public Long register(CeoReplyPhraseCreateCommand command) {
        return ceoReplyPhraseService.register(command.ceoId(), command.name(), command.content());
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

    @Override
    public void remove(CeoReplyPhraseDeleteCommand command) {
        ceoReplyPhraseService.remove(command.ceoId(), command.replyPhraseId());
    }
}
