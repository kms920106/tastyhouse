package com.tastyhouse.infrastructure.partnership.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;

/**
 * 제휴 신청 관리 목록 항목 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 상세용 형제인
 * {@link PartnershipRequestDetailResult}와 같은 패키지에 공존하지만 필드 셋이 달라(목록은 주소를
 * 담지 않는다) 통합하지 않는다. 식별자는 HTTP 경계까지 그대로 전달되는 표현용 값이므로 도메인
 * VO({@code PartnershipRequestId})가 아니라 {@code Long}으로 투영한다.
 */
public record PartnershipRequestListItemResult(
    Long id,
    String businessName,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt
) {

    @QueryProjection
    public PartnershipRequestListItemResult {
    }
}
