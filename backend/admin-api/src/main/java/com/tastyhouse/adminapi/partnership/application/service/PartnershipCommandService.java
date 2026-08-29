package com.tastyhouse.adminapi.partnership.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapi.partnership.application.port.in.PartnershipCommandUseCase;
import com.tastyhouse.adminapi.partnership.application.port.in.PartnershipDeleteCommand;
import com.tastyhouse.adminapi.partnership.application.port.in.PartnershipStatusChangeCommand;
import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 제휴 신청 관리 command 서비스.
 *
 * <p>domain write 포트({@link PartnershipRepository})만 주입해 상태 변경·삭제를 수행한다. 조회는
 * {@code PartnershipQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>{@code PartnershipRequest}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code partnershipRepository.save(partnershipRequest)}를 호출한다.
 */
@Service
@Transactional
public class PartnershipCommandService implements PartnershipCommandUseCase {

    private final PartnershipRepository partnershipRepository;

    public PartnershipCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    @Override
    public void changeStatus(PartnershipStatusChangeCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipStatus partnershipStatus = PartnershipStatus.from(command.status());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.changeStatus(partnershipStatus);
        partnershipRepository.save(partnershipRequest);
    }

    @Override
    public void deletePartnershipRequest(PartnershipDeleteCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.delete();
        partnershipRepository.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
