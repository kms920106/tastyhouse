package com.tastyhouse.webapi.partnership;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.domain.repository.PartnershipRepository;

/**
 * 제휴 신청 command 서비스.
 *
 * <p>domain write 포트({@link PartnershipRepository})만 주입해 신청 생성을 수행한다.
 *
 * <p>CQRS 규칙대로 <b>식별자만</b> 반환하며, 컨트롤러도 그 식별자를 그대로 응답한다 — 등록 응답은
 * 생성된 id 하나이므로 재조회로 상세를 조립하지 않는다(등록 API 응답 본문 규칙). 상세가 필요한
 * 클라이언트는 반환받은 id로 별도 조회를 호출한다.
 */
@Service
@Transactional
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    /**
     * @return 생성된 제휴 신청 식별자
     */
    public Long createPartnershipRequest(
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
        return saved.getPartnershipRequestId().value();
    }
}
