package com.tastyhouse.domain.partnership.repository;

import java.util.Optional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

/**
 * 제휴 신청 write 포트.
 *
 * <p>도메인 모델·VO만 주고받는 command 경로 전용 포트다. 목록·검색·페이징·상세 등 표현 목적 read는
 * infrastructure-module의 {@code partnership/query/PartnershipQueryDao}가 담당하므로 이 인터페이스에
 * 두지 않는다(CQRS 분리).
 */
public interface PartnershipRepository {

    Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId);

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}
