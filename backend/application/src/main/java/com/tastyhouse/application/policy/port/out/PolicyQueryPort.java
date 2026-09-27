package com.tastyhouse.application.policy.port.out;

import java.util.Optional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PolicyQueryPort {

    Optional<PolicyDocumentResult> findCurrentByType(PolicyType type);

    Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version);

    PageResult<PolicyListItemResult> findAllByType(PolicyType type, PageQuery pageQuery);
}
