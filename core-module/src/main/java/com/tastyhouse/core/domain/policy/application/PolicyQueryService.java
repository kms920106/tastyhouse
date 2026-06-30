package com.tastyhouse.core.domain.policy.application;

import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PolicyQueryService {

    private final PolicyDocumentRepository policyDocumentRepository;

    public PolicyDocumentResult findCurrentByType(PolicyType type) {
        return policyDocumentRepository.findCurrentByType(type)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_CURRENT_NOT_FOUND));
    }

    public PolicyDocumentResult findByTypeAndVersion(PolicyType type, String version) {
        return policyDocumentRepository.findByTypeAndVersion(type, version)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_VERSION_NOT_FOUND));
    }

    public Page<PolicyListItemResult> findAllByType(PolicyType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return policyDocumentRepository.findAllByType(type, pageable);
    }
}
