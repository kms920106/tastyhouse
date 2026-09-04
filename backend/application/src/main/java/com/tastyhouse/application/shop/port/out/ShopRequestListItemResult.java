package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청처리 현황 목록 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — package-private이면 QueryDSL {@code Projections.constructor}가
 * {@code Class#getConstructors()}(public 생성자만 반환)에서 생성자를 찾지 못해 <b>컴파일은 통과하고 그 쿼리
 * 실행 시에만 500</b>이 난다. {@code QueryResultRecordVisibilityTest}가 이를 가드한다.
 *
 * <p>첨부는 존재 여부({@code hasAttachment})만 담고 URL은 담지 않는다 — 목록에서 파일 join·URL 조립 비용을
 * 치를 이유가 없고, 다운로드는 상세에서 한다.
 */
public record ShopRequestListItemResult(
    Long requestId,
    ShopRequestType requestType,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
