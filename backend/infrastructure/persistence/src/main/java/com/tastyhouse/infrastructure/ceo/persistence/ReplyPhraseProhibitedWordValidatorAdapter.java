package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;

@Component
public class ReplyPhraseProhibitedWordValidatorAdapter implements ReplyPhraseTextValidator {
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ReplyPhraseProhibitedWordValidatorAdapter(ProhibitedWordValidator prohibitedWordValidator) {
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public void validate(String text) {
        prohibitedWordValidator.validate(text);
    }
}
