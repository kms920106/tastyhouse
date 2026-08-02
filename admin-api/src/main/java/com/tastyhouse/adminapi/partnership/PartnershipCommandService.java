package com.tastyhouse.adminapi.partnership;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.domain.partnership.domain.repository.PartnershipRepository;
import com.tastyhouse.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 제휴 신청 관리 command 서비스.
 *
 * <p>domain write 포트({@link PartnershipRepository})만 주입해 상태 변경·삭제를 수행한다. 조회는
 * {@link PartnershipQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>{@code PartnershipRequest}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code partnershipRepository.save(partnershipRequest)}를 호출한다.
 */
@Service
@Transactional
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    public void changeStatus(Long id, String status) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(id);
        PartnershipStatus partnershipStatus = PartnershipStatus.from(status);
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.changeStatus(partnershipStatus);
        partnershipRepository.save(partnershipRequest);
    }

    public void deletePartnershipRequest(Long id) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(id);
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.delete();
        partnershipRepository.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
