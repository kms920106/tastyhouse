package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.shop.domain.model.ProhibitedWord;
import com.tastyhouse.core.domain.shop.domain.repository.ProhibitedWordRepository;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 텍스트에 금칙어가 포함되어 있는지 검증하는 공용 컴포넌트.
 *
 * <p>추후 캐싱 고려: 매 호출마다 {@link ProhibitedWordRepository#findAll()}을 호출하므로
 * 금칙어 수가 많아지면 비효율적일 수 있다. 현재는 단순 구현으로 둔다.
 */
@Component
@RequiredArgsConstructor
public class ProhibitedWordValidator {

    private final ProhibitedWordRepository prohibitedWordRepository;

    public List<String> findViolations(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String lowerText = text.toLowerCase();

        return prohibitedWordRepository.findAll().stream()
            .map(ProhibitedWord::getWord)
            .filter(word -> lowerText.contains(word.toLowerCase()))
            .toList();
    }

    public void validate(String text) {
        List<String> violations = findViolations(text);

        if (!violations.isEmpty()) {
            throw new BusinessException(
                ErrorCode.SHOP_TEXT_PROHIBITED_WORD,
                ErrorCode.SHOP_TEXT_PROHIBITED_WORD.getDefaultMessage() + ": " + String.join(", ", violations)
            );
        }
    }
}
