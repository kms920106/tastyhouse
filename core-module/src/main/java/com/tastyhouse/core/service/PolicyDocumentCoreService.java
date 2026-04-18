package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.policy.PolicyDocument;
import com.tastyhouse.core.entity.policy.PolicyType;
import com.tastyhouse.core.entity.policy.dto.PolicyDocumentDto;
import com.tastyhouse.core.entity.policy.dto.PolicyListItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.policy.PolicyDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyDocumentCoreService {

    private final PolicyDocumentRepository policyDocumentRepository;

    @Transactional(readOnly = true)
    public Optional<PolicyDocumentDto> findCurrentByType(PolicyType type) {
        return policyDocumentRepository.findCurrentByType(type);
    }

    @Transactional(readOnly = true)
    public Optional<PolicyDocumentDto> findByTypeAndVersion(PolicyType type, String version) {
        return policyDocumentRepository.findByTypeAndVersion(type, version);
    }

    @Transactional(readOnly = true)
    public Page<PolicyListItemDto> findAllByTypeWithPagination(PolicyType type, int page, int size) {
        return policyDocumentRepository.findAllByType(type, PageRequest.of(page, size));
    }

    @Transactional
    public PolicyDocument create(
        PolicyType type,
        String version,
        String title,
        String content,
        Boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        PolicyDocument policyDocument =
            PolicyDocument.of(
                type,
                version,
                title,
                content,
                false,
                mandatory,
                effectiveDate, createdBy
            );

        return policyDocumentRepository.save(policyDocument);
    }

    @Transactional
    public void updateCurrentPolicy(Long newPolicyId) {
        PolicyDocument newPolicy = policyDocumentRepository.findById(newPolicyId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_NOT_FOUND));

        Optional<PolicyDocument> currentPolicy = policyDocumentRepository
            .findCurrentEntityByType(newPolicy.getType());

        currentPolicy.ifPresent(policy -> policy.updateCurrent(false));

        newPolicy.updateCurrent(true);
    }

    @Transactional
    public void update(Long id, String title, String content, Boolean mandatory,
                      LocalDateTime effectiveDate, String updatedBy) {
        PolicyDocument policyDocument = policyDocumentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_NOT_FOUND));

        policyDocument.update(title, content, mandatory, effectiveDate, updatedBy);
    }
}
