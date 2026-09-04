package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청처리 현황 목록 항목 — 조회 결과에 계약 변경 여부를 더한 형태.
 *
 * <p><b>챕터 09</b>에서 신설. {@code contractAmending}은 {@code ShopRequestType#isContractAmending}이
 * 판정하는데 그 메서드는 api 모듈에 허용된 읽기 accessor 3종
 * ({@code name}·{@code getDescription}·{@code getDisplayName})이 아닌 <b>도메인 로직</b>이라
 * ({@code apiModuleShouldOnlyReadDomainEnums}) 호출이 application에 남아야 한다.
 *
 * <p>{@code requestType}·{@code status} 자체는 그대로 넘겨 표현 계약이
 * {@code name()}·{@code getDescription()}으로 강등한다(챕터 07의 정상 경로).
 */
public record ShopRequestListItemViewResult(
    Long requestId,
    ShopRequestType requestType,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    boolean contractAmending,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
