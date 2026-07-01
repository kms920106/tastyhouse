package com.tastyhouse.core.domain.partnership.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipRequestResult create(String businessName, String address, String addressDetail,
                                           String contactName, String contactPhone,
                                           LocalDateTime consultationRequestedAt) {
        PartnershipRequest saved = partnershipRepository.save(
            PartnershipRequest.of(businessName, address, addressDetail, contactName, contactPhone, consultationRequestedAt)
        );
        return PartnershipRequestResult.from(saved);
    }
}
