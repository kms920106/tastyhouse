package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidatorPort;

@Service
public class ReplyPhraseProhibitedWordValidatorAdapter implements ReplyPhraseTextValidatorPort {

    private final ProhibitedWordValidator prohibitedWordValidator;

    public ReplyPhraseProhibitedWordValidatorAdapter(ProhibitedWordValidator prohibitedWordValidator) {
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public void validate(String text) {
        prohibitedWordValidator.validate(text);
    }
}
