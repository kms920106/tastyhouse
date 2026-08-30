package com.tastyhouse.adminapplication.partnership.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.partnership.port.out.PartnershipQueryPort;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.partnership.port.out.PartnershipSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.partnership.response.PartnershipRequestDetailResponse;
import com.tastyhouse.adminapplication.partnership.response.PartnershipRequestListItemResponse;
import com.tastyhouse.adminapplication.partnership.port.in.PartnershipQueryUseCase;

/**
 * 제휴 신청 관리 조회 서비스.
 *
 * <p>읽기 포트({@link PartnershipQueryPort})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link PartnershipCommandService}가 담당한다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 상태값은 여기서 {@code PartnershipStatus.from}으로 승격하고,
 * Response로 내보낼 때는 다시 {@code name()} 문자열로 되돌린다(api 모듈은 core enum을 노출하지 않는다).
 */
@Service
@Transactional(readOnly = true)
public class PartnershipQueryService implements PartnershipQueryUseCase {

    private final PartnershipQueryPort partnershipQueryPort;

    public PartnershipQueryService(PartnershipQueryPort partnershipQueryPort) {
        this.partnershipQueryPort = partnershipQueryPort;
    }

    @Override
    public PaginationResponse<PartnershipRequestListItemResponse> getPartnershipRequests(
        String businessName,
        String contactName,
        String contactPhone,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    ) {
        PartnershipStatus partnershipStatus = status == null ? null : PartnershipStatus.from(status);
        PartnershipSearchCondition condition = PartnershipSearchCondition.of(businessName, contactName, contactPhone, partnershipStatus, startDate, endDate);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<PartnershipRequestListItemResponse> pageResult = partnershipQueryPort.findPartnershipRequests(condition, pageQuery)
            .map(this::toPartnershipRequestListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public PartnershipRequestDetailResponse getPartnershipRequest(Long id) {
        PartnershipRequestDetailResult detail = partnershipQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
        return toPartnershipRequestDetailResponse(detail);
    }

    private PartnershipRequestListItemResponse toPartnershipRequestListItemResponse(PartnershipRequestListItemResult dto) {
        return PartnershipRequestListItemResponse.from(
            dto.id(),
            dto.businessName(),
            dto.contactName(),
            dto.contactPhone(),
            dto.status() != null ? dto.status().name() : null,
            dto.consultationRequestedAt(),
            dto.createdAt()
        );
    }

    private PartnershipRequestDetailResponse toPartnershipRequestDetailResponse(PartnershipRequestDetailResult dto) {
        return PartnershipRequestDetailResponse.from(
            dto.id(),
            dto.businessName(),
            dto.address(),
            dto.addressDetail(),
            dto.contactName(),
            dto.contactPhone(),
            dto.status() != null ? dto.status().name() : null,
            dto.consultationRequestedAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
