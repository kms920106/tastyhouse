package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidator;

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
