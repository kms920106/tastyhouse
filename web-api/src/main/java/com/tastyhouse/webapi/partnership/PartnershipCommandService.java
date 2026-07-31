package com.tastyhouse.webapi.partnership;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.domain.repository.PartnershipRepository;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;

/**
 * 제휴 신청 command 서비스.
 *
 * <p>domain write 포트({@link PartnershipRepository})만 주입해 신청 생성을 수행한다. 회원 화면에는
 * 제휴 신청 조회가 없어 이 도메인의 web-api 쪽에는 QueryService를 두지 않는다(관리 조회는 admin-api의
 * {@code PartnershipQueryService}가 담당).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipRequestResponse createPartnershipRequest(
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt
    ) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            businessName, address, addressDetail, contactName, contactPhone, consultationRequestedAt
        );
        PartnershipRequest saved = partnershipRepository.save(partnershipRequest);
        return toPartnershipRequestResponse(saved);
    }

    private PartnershipRequestResponse toPartnershipRequestResponse(PartnershipRequest partnershipRequest) {
        return PartnershipRequestResponse.from(
            partnershipRequest.getPartnershipRequestId().value(),
            partnershipRequest.getBusinessName(),
            partnershipRequest.getAddress(),
            partnershipRequest.getAddressDetail(),
            partnershipRequest.getContactName(),
            partnershipRequest.getContactPhone(),
            partnershipRequest.getConsultationRequestedAt(),
            partnershipRequest.getCreatedAt()
        );
    }
}
