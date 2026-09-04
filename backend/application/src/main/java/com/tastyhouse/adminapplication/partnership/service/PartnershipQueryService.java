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
import com.tastyhouse.adminapplication.partnership.port.in.PartnershipQueryUseCase;

/**
 * 제휴 신청 관리 조회 서비스.
 *
 * <p>읽기 포트({@link PartnershipQueryPort})만 주입해 조회한다. write 포트를 주입하지 않으며,
 * 쓰기는 {@link PartnershipCommandService}가 담당한다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 상태값은 여기서 {@code PartnershipStatus.from}으로 승격한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class PartnershipQueryService implements PartnershipQueryUseCase {

    private final PartnershipQueryPort partnershipQueryPort;

    public PartnershipQueryService(PartnershipQueryPort partnershipQueryPort) {
        this.partnershipQueryPort = partnershipQueryPort;
    }

    @Override
    public PageResult<PartnershipRequestListItemResult> getPartnershipRequests(
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
        return partnershipQueryPort.findPartnershipRequests(condition, pageQuery);
    }

    @Override
    public PartnershipRequestDetailResult getPartnershipRequest(Long id) {
        return partnershipQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
