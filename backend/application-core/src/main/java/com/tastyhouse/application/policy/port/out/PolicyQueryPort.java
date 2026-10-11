package com.tastyhouse.application.policy.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PolicyQueryPort {

    Optional<PolicyDocumentResult> findCurrentByType(String type);

    Optional<PolicyDocumentResult> findByTypeAndVersion(String type, String version);

    PageResult<PolicyListItemResult> findAllByType(String type, PageQuery pageQuery);
}
