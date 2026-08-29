package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청처리 현황 상세 조회 결과(인덱스 부분).
 *
 * <p>반드시 {@code public}이어야 한다 — 이유는 {@code ShopRequestListItemResult} Javadoc 참조.
 *
 * <p>{@code shopId}는 응답에 노출하지 않지만 투영한다 — 스레드가 경로의 가게에 속하는지 재검증하는 데 쓴다
 * (불일치는 403이 아니라 404다). {@code sourceRequestId}는 유형별 원본을 다시 투영하기 위한 값이고,
 * {@code status}·{@code rejectReason}은 <b>여기 값이 아니라 원본 애그리거트 값으로</b> 응답한다(인덱스는
 * 파생 읽기모델이라 진실원이 아니다).
 *
 * <p>{@code attachmentUrl}은 DAO가 {@code UPLOADED_FILE}을 조인해 {@code FileUrlResolver}로 완성한 표시용
 * URL이다.
 */
public record ShopRequestDetailResult(
    Long requestId,
    Long shopId,
    ShopRequestType requestType,
    Long sourceRequestId,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    String attachmentUrl,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
