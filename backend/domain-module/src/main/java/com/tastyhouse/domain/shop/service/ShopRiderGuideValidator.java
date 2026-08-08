package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.Shop;

/**
 * 라이더 가게방문 안내 문구 등록 기준 검증 정책(도메인 서비스).
 *
 * <p>배민 사장님광장이 규정한 "작성 불가 3유형"을 코드 규칙으로 옮긴다 — (1) 금칙어,
 * (2) 가게 실주소 재기재, (3) 배차·이동수단 특정. 금칙어는 {@link ProhibitedWordValidator}에
 * 위임하고 나머지 2유형을 자체 판정한다.
 *
 * <p>액터(ceo/admin)와 무관하게 같은 규칙이 적용되어야 하는 무상태 정책이므로 도메인 계층에 둔다.
 * {@code @Component} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의 {@code DomainServiceConfig}가
 * 담당한다.
 */
public class ShopRiderGuideValidator {

    private static final int VISIT_GUIDE_MAX_LENGTH = 200;

    /**
     * 주소 재기재 판정에 쓰는 연속 일치 토큰 수.
     *
     * <p>토큰 하나("서울시", "강남구")만으로 판정하면 "강남구청 뒷골목" 같은 정상 안내가 오탐으로 막힌다.
     * 반대로 전체 문자열 완전 일치만 보면 "서울시 송파구 위례성대로"처럼 주소 일부만 적은 케이스를 놓친다.
     */
    private static final int ADDRESS_MATCH_TOKEN_THRESHOLD = 2;

    /**
     * 배차·이동수단 특정 키워드 사전.
     *
     * <p><b>왜 {@code PROHIBITED_WORD} 테이블에 넣지 않는가</b>: 그 테이블은 가게소개·찾아오는길과
     * 공유되며({@link ProhibitedWordValidator}가 전량 로드), 거기에 "보온가방"을 넣으면 가게소개에서도
     * 차단된다. 라이더 안내에서만 금지되는 어휘이므로 별도 사전으로 둔다. 운영 중 조정이 잦아지면
     * {@code PROHIBITED_WORD}에 {@code scope} 컬럼을 추가하는 방향으로 승격하는 것이 후속 과제다.
     */
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

    /**
     * 사전 검수용 — 위반 사유를 예외 없이 목록으로 반환한다. 프론트가 저장 실패 토스트가 아니라
     * 인라인 위반 목록으로 보여주기 위한 경로이므로, 길이 초과도 함께 포함시킨다.
     */
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

    /**
     * 저장 전 게이트 — 위반이 있으면 첫 위반 유형에 해당하는 예외를 던진다.
     *
     * <p>길이 초과는 도메인 모델({@code ShopRiderGuide#changeVisitGuide})이 최종 판정하므로 여기서는
     * 검사하지 않는다.
     */
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

    /**
     * 가게 실주소(도로명·지번)의 토큰이 안내 문구에 {@value #ADDRESS_MATCH_TOKEN_THRESHOLD}개 이상
     * 연속으로 등장하는지 판정한다.
     */
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
