package com.tastyhouse.core.repository.policy;

import com.tastyhouse.core.entity.policy.PolicyDocument;
import com.tastyhouse.core.entity.policy.PolicyType;
import com.tastyhouse.core.entity.policy.dto.PolicyDocumentDto;
import com.tastyhouse.core.entity.policy.dto.PolicyListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PolicyDocumentRepository {

    Optional<PolicyDocument> findById(Long id);

    Optional<PolicyDocument> findCurrentEntityByType(PolicyType type);

    Optional<PolicyDocumentDto> findCurrentByType(PolicyType type);

    Optional<PolicyDocumentDto> findByTypeAndVersion(PolicyType type, String version);

    Page<PolicyListItemDto> findAllByType(PolicyType type, Pageable pageable);

    PolicyDocument save(PolicyDocument policyDocument);
}
