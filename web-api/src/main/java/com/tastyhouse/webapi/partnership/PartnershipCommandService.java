package com.tastyhouse.webapi.partnership;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.domain.repository.PartnershipRepository;

/**
 * 제휴 신청 command 서비스.
 *
 * <p>domain write 포트({@link PartnershipRepository})만 주입해 신청 생성을 수행한다.
 *
 * <p>CQRS 규칙대로 <b>식별자만</b> 반환한다 — 신청 응답 조립은 커밋 이후 컨트롤러가
 * {@link PartnershipQueryService}로 재조회해 담당한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

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
