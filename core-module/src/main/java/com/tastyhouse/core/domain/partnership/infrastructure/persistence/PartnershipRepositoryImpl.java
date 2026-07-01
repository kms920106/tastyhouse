package com.tastyhouse.core.domain.partnership.infrastructure.persistence;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PartnershipRepositoryImpl implements PartnershipRepository {

    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    @Override
    public PartnershipRequest save(PartnershipRequest request) {
        return partnershipRequestJpaRepository.save(request);
    }
}
