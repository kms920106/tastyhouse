package com.tastyhouse.webapplication.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.webapplication.partnership.port.in.PartnershipCommandUseCase;
import com.tastyhouse.webapplication.partnership.port.in.PartnershipRequestCreateCommand;

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
public class PartnershipCommandService implements PartnershipCommandUseCase {

    private final PartnershipRepository partnershipRepository;

    public PartnershipCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    /**
     * @return 생성된 제휴 신청 식별자
     */
    @Override
    public Long createPartnershipRequest(PartnershipRequestCreateCommand command) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            command.businessName(), command.address(), command.addressDetail(),
            command.contactName(), command.contactPhone(), command.consultationRequestedAt()
        );
        PartnershipRequest saved = partnershipRepository.save(partnershipRequest);
        return saved.getPartnershipRequestId().value();
    }
}
