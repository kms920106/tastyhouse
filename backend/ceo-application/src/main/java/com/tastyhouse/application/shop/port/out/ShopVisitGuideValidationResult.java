package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 라이더 방문안내 사전검증 결과 — 등록 가능 여부와 위반 사유 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 판정은 도메인 서비스({@code ShopRiderGuideValidator})가 애그리거트를
 * 받아 수행하므로 application에 남아야 한다 — 표현 계약은 도메인 모델도 도메인 서비스도 알 수 없다
 * ({@code apiModuleShouldBeDomainModelFree}). 그 결과만 이 record에 담아 넘긴다.
 */
public record ShopVisitGuideValidationResult(
    boolean valid,
    List<String> violations
) {
}
