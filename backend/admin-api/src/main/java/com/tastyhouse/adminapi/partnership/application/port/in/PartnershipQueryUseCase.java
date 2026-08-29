package com.tastyhouse.adminapi.partnership.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.adminapi.partnership.adapter.in.web.response.PartnershipRequestDetailResponse;
import com.tastyhouse.adminapi.partnership.adapter.in.web.response.PartnershipRequestListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 제휴 문의 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PartnershipQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface PartnershipQueryUseCase {

    PaginationResponse<PartnershipRequestListItemResponse> getPartnershipRequests(
        String businessName,
        String contactName,
        String contactPhone,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    );

    PartnershipRequestDetailResponse getPartnershipRequest(Long id);
}
