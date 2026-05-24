package com.tastyhouse.core.domain.policy.domain.repository;

import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PolicyDocumentRepository {

    Optional<PolicyDocument> findById(PolicyDocumentId id);

    Optional<PolicyDocument> findCurrentEntityByType(PolicyType type);

    Optional<PolicyDocumentResult> findCurrentByType(PolicyType type);

    Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version);

    Page<PolicyListItemResult> findAllByType(PolicyType type, Pageable pageable);

    PolicyDocument save(PolicyDocument policyDocument);
}
