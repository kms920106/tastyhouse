package com.tastyhouse.application.policy.port.out;

import java.util.Optional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface PolicyQueryPort {

    Optional<PolicyDocumentResult> findCurrentByType(PolicyType type);

    Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version);

    PageResult<PolicyListItemResult> findAllByType(PolicyType type, PageQuery pageQuery);
}
