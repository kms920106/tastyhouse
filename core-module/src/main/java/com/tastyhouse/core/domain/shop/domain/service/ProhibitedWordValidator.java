package com.tastyhouse.core.domain.shop.domain.service;

import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.ProhibitedWord;
import com.tastyhouse.core.domain.shop.domain.repository.ProhibitedWordRepository;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 텍스트에 금칙어가 포함되어 있는지 검증하는 무상태 정책(도메인 서비스).
 *
 * <p>점주가 입력하는 가게소개·찾아오는길 등의 텍스트를 저장 전에 검수한다. 액터(ceo/admin)와 무관하게
 * 같은 규칙이 적용되어야 하는 무상태 정책(분류 D)이므로 도메인 계층에 둔다.
 *
 * <p>{@code @Component} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다.
 *
 * <p>추후 캐싱 고려: 매 호출마다 {@link ProhibitedWordRepository#findAll()}을 호출하므로
 * 금칙어 수가 많아지면 비효율적일 수 있다. 현재는 단순 구현으로 둔다.
 */
public class ProhibitedWordValidator {

    private final ProhibitedWordRepository prohibitedWordRepository;

    public ProhibitedWordValidator(ProhibitedWordRepository prohibitedWordRepository) {
        this.prohibitedWordRepository = prohibitedWordRepository;
    }

    public List<String> findViolations(String text) {
        if (text == null || text.isBlank()) {
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
