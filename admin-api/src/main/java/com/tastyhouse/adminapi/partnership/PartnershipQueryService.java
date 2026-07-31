package com.tastyhouse.adminapi.partnership;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.partnership.query.PartnershipQueryDao;
import com.tastyhouse.infrastructure.partnership.query.PartnershipRequestDetailResult;
import com.tastyhouse.infrastructure.partnership.query.PartnershipRequestListItemResult;
import com.tastyhouse.infrastructure.partnership.query.PartnershipSearchCondition;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestDetailResponse;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestListItemResponse;

/**
 * 제휴 신청 관리 조회 서비스.
 *
 * <p>infra read 어댑터({@link PartnershipQueryDao})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link PartnershipCommandService}가 담당한다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 상태값은 여기서 {@code PartnershipStatus.from}으로 승격하고,
 * Response로 내보낼 때는 다시 {@code name()} 문자열로 되돌린다(api 모듈은 core enum을 노출하지 않는다).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnershipQueryService {

    private final PartnershipQueryDao partnershipQueryDao;

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
        PageResult<PartnershipRequestListItemResponse> pageResult = partnershipQueryDao.findPartnershipRequests(condition, pageQuery)
            .map(this::toPartnershipRequestListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public PartnershipRequestDetailResponse getPartnershipRequest(Long id) {
        PartnershipRequestDetailResult detail = partnershipQueryDao.findDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
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
