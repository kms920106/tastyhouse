package com.tastyhouse.webapi.partnership;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.infrastructure.partnership.query.PartnershipQueryDao;
import com.tastyhouse.infrastructure.partnership.query.PartnershipRequestDetailResult;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;

/**
 * 제휴 신청 조회 서비스(web, CQRS query 측).
 *
 * <p>회원 화면에는 제휴 신청 목록·상세 엔드포인트가 없고, 이 서비스는 <b>신청 직후 응답 조립</b>만을 위해
 * 존재한다 — {@link PartnershipCommandService}가 CQRS 규칙대로 식별자만 반환하므로, 컨트롤러가 커밋
 * 이후 이 서비스로 재조회해 기존과 동일한 {@link PartnershipRequestResponse}를 만든다.
 *
 * <p>관리 조회는 admin-api의 {@code PartnershipQueryService}가 담당하며, 모듈이 달라 이름이 겹쳐도
 * 충돌하지 않는다(소비자별 조회 범위·응답 형태가 달라 통합하지 않는다).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnershipQueryService {

    private final PartnershipQueryDao partnershipQueryDao;

    /**
     * 제휴 신청 응답 — 명령이 돌려준 식별자로 커밋 이후 재조회해 조립한다.
     */
    public PartnershipRequestResponse getPartnershipRequestResponse(Long id) {
        PartnershipRequestDetailResult detail = partnershipQueryDao.findDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "제휴 신청을 찾을 수 없습니다."));

        return toPartnershipRequestResponse(detail);
    }

    private PartnershipRequestResponse toPartnershipRequestResponse(PartnershipRequestDetailResult dto) {
        return PartnershipRequestResponse.from(
            dto.id(),
            dto.businessName(),
            dto.address(),
            dto.addressDetail(),
            dto.contactName(),
            dto.contactPhone(),
            dto.consultationRequestedAt(),
            dto.createdAt()
        );
    }
}
