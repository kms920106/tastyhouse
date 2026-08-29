package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;

/**
 * 매장 가격 인증 요청 검수 목록 투영.
 *
 * <p>{@code shopName}은 {@code SHOP_STORE_PRICE_VERIFICATION}에 없어 {@code SHOP}을 조인해 담는다 —
 * 관리자 검수 목록이 어느 가게 요청인지 보여야 한다.
 *
 * <p>{@code priceListFileUrl}은 조인 직후에는 <b>저장 경로</b>가 담기고, DAO가
 * {@code FileUrlResolver}로 표시용 URL로 바꿔 내보낸다({@code ProductImageChangeRequestResult}와 같은
 * 관례) — api 모듈에서 파일 변환 호출이 사라지게 하려는 의도다.
 *
 * <p>{@code itemCount}를 목록에 담는 이유는 검수자가 상세를 열기 전에 <b>작업량</b>을 가늠해야 하기
 * 때문이다. 항목 자체는 상세 조회가 담당한다.
 */
public record StorePriceVerificationListItemResult(
    Long id,
    Long shopId,
    String shopName,
    StorePriceVerificationStatus status,
    String priceListFileUrl,
    String rejectReason,
    Long itemCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
