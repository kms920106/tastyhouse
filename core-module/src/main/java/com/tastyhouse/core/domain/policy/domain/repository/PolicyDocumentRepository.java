package com.tastyhouse.core.domain.policy.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface PolicyDocumentRepository {

    Optional<PolicyDocument> findById(PolicyDocumentId id);

    Optional<PolicyDocument> findCurrentEntityByType(PolicyType type);

    Optional<PolicyDocumentResult> findCurrentByType(PolicyType type);

    Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version);

    PageResult<PolicyListItemResult> findAllByType(PolicyType type, PageQuery pageQuery);

    PolicyDocument save(PolicyDocument policyDocument);
}
