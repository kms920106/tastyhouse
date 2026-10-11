package com.tastyhouse.application.shop.service;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordLoadPort;

public class ProhibitedWordValidator {

    private final ProhibitedWordLoadPort prohibitedWordLoadPort;

    public ProhibitedWordValidator(ProhibitedWordLoadPort prohibitedWordLoadPort) {
        this.prohibitedWordLoadPort = prohibitedWordLoadPort;
    }

    public List<String> findViolations(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String lowerText = text.toLowerCase();

        return prohibitedWordLoadPort.findAll().stream()
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
