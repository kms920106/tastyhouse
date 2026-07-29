package com.tastyhouse.infrastructure.partnership.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;

/**
 * 제휴 신청 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 목록용 형제인
 * {@link PartnershipRequestListItemResult}와 달리 주소·수정 시각까지 담는다. 식별자는 HTTP
 * 경계까지 그대로 전달되는 표현용 값이므로 도메인 VO({@code PartnershipRequestId})가 아니라
 * {@code Long}으로 투영한다.
 */
public record PartnershipRequestDetailResult(
    Long id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @QueryProjection
    public PartnershipRequestDetailResult {
    }
}
