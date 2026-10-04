package com.tastyhouse.application.shop.service;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;

public class ProhibitedWordValidator {

    private final ProhibitedWordPersistencePort prohibitedWordPersistencePort;

    public ProhibitedWordValidator(ProhibitedWordPersistencePort prohibitedWordPersistencePort) {
        this.prohibitedWordPersistencePort = prohibitedWordPersistencePort;
    }

    public List<String> findViolations(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String lowerText = text.toLowerCase();

        return prohibitedWordPersistencePort.findAll().stream()
            .map(ProhibitedWord::getWord)
            .filter(word -> lowerText.contains(word.toLowerCase()))
            .toList();
    }

    public void validate(String text) {
        List<String> violations = findViolations(text);

        if (!violations.isEmpty()) {
            throw new ApplicationException(
                ApplicationErrorCode.SHOP_TEXT_PROHIBITED_WORD,
                ApplicationErrorCode.SHOP_TEXT_PROHIBITED_WORD.getDefaultMessage() + ": " + String.join(", ", violations)
            );
        }
    }
}
