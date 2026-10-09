package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;
import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidator;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseLoadPort;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseSavePort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class CeoReplyPhraseService {

    private static final int MAX_PHRASE_COUNT = 5;

    private final CeoReplyPhraseLoadPort ceoReplyPhraseLoadPort;
    private final CeoReplyPhraseSavePort ceoReplyPhraseSavePort;
    private final ReplyPhraseTextValidator replyPhraseTextValidator;

    public CeoReplyPhraseService(
        CeoReplyPhraseLoadPort ceoReplyPhraseLoadPort,
        CeoReplyPhraseSavePort ceoReplyPhraseSavePort,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        this.ceoReplyPhraseLoadPort = ceoReplyPhraseLoadPort;
        this.ceoReplyPhraseSavePort = ceoReplyPhraseSavePort;
        this.replyPhraseTextValidator = replyPhraseTextValidator;
    }

    public Long register(Long ceoId, String name, String content) {
        CeoId ownerId = CeoId.of(ceoId);
        replyPhraseTextValidator.validate(content);

        long count = ceoReplyPhraseLoadPort.countByCeoId(ownerId);
        if (count >= MAX_PHRASE_COUNT) {
            throw new ApplicationException(CeoErrorCode.CEO_REPLY_PHRASE_LIMIT_EXCEEDED);
        }

        CeoReplyPhrase saved = ceoReplyPhraseSavePort.save(
            CeoReplyPhrase.of(ownerId, name, content, (int) count)
        );
        return saved.getId();
    }

    public void modify(Long ceoId, Long phraseId, String name, String content) {
        replyPhraseTextValidator.validate(content);

        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        phrase.updateContent(name, content);
        ceoReplyPhraseSavePort.save(phrase);
    }

    public void remove(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        ceoReplyPhraseSavePort.delete(phrase);
    }

    private CeoReplyPhrase loadOwnPhrase(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = ceoReplyPhraseLoadPort.findById(CeoReplyPhraseId.of(phraseId))
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.CEO_REPLY_PHRASE_NOT_FOUND));
        if (!phrase.getCeoId().equals(CeoId.of(ceoId))) {
            throw new ApplicationException(CeoErrorCode.CEO_REPLY_PHRASE_ACCESS_DENIED);
        }
        return phrase;
    }
}
