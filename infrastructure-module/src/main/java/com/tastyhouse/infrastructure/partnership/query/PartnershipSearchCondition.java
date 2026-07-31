package com.tastyhouse.infrastructure.partnership.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.partnership.domain.model.PartnershipStatus;

/**
 * 제휴 신청 관리 목록 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다.
 * 소비 모듈(admin-api)의 {@code PartnershipQueryService}가 원시 파라미터를 받아
 * {@code PartnershipStatus.from(String)}으로 승격한 뒤 조립해 전달한다(api 모듈은 core enum을
 * HTTP 경계에 노출하지 않는다).
 */
public record PartnershipSearchCondition(
    String businessName,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static PartnershipSearchCondition of(
        String businessName,
        String contactName,
        String contactPhone,
        PartnershipStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new PartnershipSearchCondition(businessName, contactName, contactPhone, status, startDate, endDate);
    }
}
