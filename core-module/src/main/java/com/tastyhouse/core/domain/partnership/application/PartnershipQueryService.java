package com.tastyhouse.core.domain.partnership.application;

import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnershipQueryService {

    private final PartnershipRepository partnershipRepository;

    public List<PartnershipRequestResult> findAll() {
        return partnershipRepository.findAllOrderByCreatedAtDesc().stream()
            .map(PartnershipRequestResult::from)
            .toList();
    }

    public Page<PartnershipRequestResult> findAllWithPagination(int page, int size) {
        return partnershipRepository.findAllOrderByCreatedAtDesc(PageRequest.of(page, size))
            .map(PartnershipRequestResult::from);
    }
}
