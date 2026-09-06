package com.tastyhouse.domain.ceo.service;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class CeoReplyPhraseService {
    private static final int MAX_PHRASE_COUNT = 5;

    private final CeoReplyPhraseRepository ceoReplyPhraseRepository;
    private final ReplyPhraseTextValidator replyPhraseTextValidator;

    public CeoReplyPhraseService(
        CeoReplyPhraseRepository ceoReplyPhraseRepository,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        this.ceoReplyPhraseRepository = ceoReplyPhraseRepository;
        this.replyPhraseTextValidator = replyPhraseTextValidator;
    }

    public Long register(Long ceoId, String name, String content) {
        CeoId ownerId = CeoId.of(ceoId);
        replyPhraseTextValidator.validate(content);

        long count = ceoReplyPhraseRepository.countByCeoId(ownerId);
        if (count >= MAX_PHRASE_COUNT) {
            throw new BusinessException(ErrorCode.CEO_REPLY_PHRASE_LIMIT_EXCEEDED);
        }

        CeoReplyPhrase saved = ceoReplyPhraseRepository.save(
            CeoReplyPhrase.of(ownerId, name, content, (int) count)
        );
        return saved.getId();
    }

    public void modify(Long ceoId, Long phraseId, String name, String content) {
        replyPhraseTextValidator.validate(content);

        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        phrase.updateContent(name, content);
        ceoReplyPhraseRepository.save(phrase);
    }

    public void remove(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        ceoReplyPhraseRepository.delete(phrase);
    }

    private CeoReplyPhrase loadOwnPhrase(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = ceoReplyPhraseRepository.findById(CeoReplyPhraseId.of(phraseId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CEO_REPLY_PHRASE_NOT_FOUND));
        if (!phrase.getCeoId().equals(CeoId.of(ceoId))) {
            throw new BusinessException(ErrorCode.CEO_REPLY_PHRASE_ACCESS_DENIED);
        }
        return phrase;
    }
}
