package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 가게소개 금칙어 사전검증 결과 — 등록 가능 여부와 위반 단어 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 판정은 도메인 서비스 {@code ProhibitedWordValidator}가 수행하므로
 * application에 남아야 하고(표현 계약이 도메인 서비스를 호출할 수 없다), 표현 계약이
 * {@code from(Result)} 한 번으로 끝낼 수 있도록 그 결과를 이 record에 담는다.
 */
public record ShopIntroductionValidationResult(
    boolean valid,
    List<String> violations
) {
}
