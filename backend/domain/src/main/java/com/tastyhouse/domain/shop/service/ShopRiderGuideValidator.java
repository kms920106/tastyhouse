package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.Shop;

public class ShopRiderGuideValidator {
    private static final int VISIT_GUIDE_MAX_LENGTH = 200;

    private static final int ADDRESS_MATCH_TOKEN_THRESHOLD = 2;

    private static final List<String> DISPATCH_RESTRICTION_KEYWORDS = List.of(
        "자동차 라이더",
        "오토바이 라이더",
        "도보 라이더",
        "배차 자제",
        "배차 제한",
        "보온가방",
        "픽업가능하신 분",
        "픽업 가능하신 분",
        "수행 부탁",
        "잡지 말",
        "잡지마",
        "받지 말아",
        "받지마"
    );

    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopRiderGuideValidator(ProhibitedWordValidator prohibitedWordValidator) {
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    public List<String> findViolations(Shop shop, String visitGuide) {
        List<String> violations = new ArrayList<>();

        if (visitGuide == null || visitGuide.isBlank()) {
            return violations;
        }

        if (visitGuide.length() > VISIT_GUIDE_MAX_LENGTH) {
            violations.add(ErrorCode.SHOP_RIDER_VISIT_GUIDE_TOO_LONG.getDefaultMessage());
        }

        List<String> prohibitedWords = prohibitedWordValidator.findViolations(visitGuide);
        if (!prohibitedWords.isEmpty()) {
            violations.add(ErrorCode.SHOP_TEXT_PROHIBITED_WORD.getDefaultMessage()
                + ": " + String.join(", ", prohibitedWords));
        }

        if (containsShopAddress(shop, visitGuide)) {
            violations.add(ErrorCode.SHOP_RIDER_VISIT_GUIDE_CONTAINS_ADDRESS.getDefaultMessage());
        }

        String dispatchKeyword = findDispatchRestrictionKeyword(visitGuide);
        if (dispatchKeyword != null) {
            violations.add(ErrorCode.SHOP_RIDER_VISIT_GUIDE_DISPATCH_RESTRICTION.getDefaultMessage()
                + ": " + dispatchKeyword);
        }

        return violations;
    }

    public void validate(Shop shop, String visitGuide) {
        if (visitGuide == null || visitGuide.isBlank()) {
            return;
        }

        prohibitedWordValidator.validate(visitGuide);

        if (containsShopAddress(shop, visitGuide)) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_CONTAINS_ADDRESS);
        }

        String dispatchKeyword = findDispatchRestrictionKeyword(visitGuide);
        if (dispatchKeyword != null) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_DISPATCH_RESTRICTION,
                ErrorCode.SHOP_RIDER_VISIT_GUIDE_DISPATCH_RESTRICTION.getDefaultMessage() + ": " + dispatchKeyword);
        }
    }

    private boolean containsShopAddress(Shop shop, String visitGuide) {
        if (shop == null) {
            return false;
        }
        return containsConsecutiveAddressTokens(shop.getRoadAddress(), visitGuide)
            || containsConsecutiveAddressTokens(shop.getLotAddress(), visitGuide);
    }

    private boolean containsConsecutiveAddressTokens(String address, String visitGuide) {
        if (address == null || address.isBlank()) {
            return false;
        }

        List<String> tokens = Arrays.stream(address.trim().split("\\s+"))
            .filter(token -> !token.isBlank())
            .toList();
        if (tokens.size() < ADDRESS_MATCH_TOKEN_THRESHOLD) {
            return false;
        }

        for (int start = 0; start <= tokens.size() - ADDRESS_MATCH_TOKEN_THRESHOLD; start++) {
            String phrase = String.join(" ", tokens.subList(start, start + ADDRESS_MATCH_TOKEN_THRESHOLD));
            if (visitGuide.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    private String findDispatchRestrictionKeyword(String visitGuide) {
        return DISPATCH_RESTRICTION_KEYWORDS.stream()
            .filter(visitGuide::contains)
            .findFirst()
            .orElse(null);
    }
}
