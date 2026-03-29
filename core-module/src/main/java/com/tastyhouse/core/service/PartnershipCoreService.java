package com.tastyhouse.core.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.partnership.PartnershipRequest;
import com.tastyhouse.core.repository.partnership.PartnershipRepository;
import com.tastyhouse.core.repository.partnership.PartnershipRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnershipCoreService {

    private final PartnershipRepository partnershipRepository;
    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    @Transactional(readOnly = true)
    public List<PartnershipRequest> findAll() {
        return partnershipRepository.findAllOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PageResult<PartnershipRequest> findAllWithPagination(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PartnershipRequest> partnershipPage = partnershipRepository.findAllOrderByCreatedAtDesc(pageRequest);
        return PageResult.from(partnershipPage);
    }

    @Transactional
    public PartnershipRequest save(PartnershipRequest partnershipRequest) {
        return partnershipRequestJpaRepository.save(partnershipRequest);
    }
}
