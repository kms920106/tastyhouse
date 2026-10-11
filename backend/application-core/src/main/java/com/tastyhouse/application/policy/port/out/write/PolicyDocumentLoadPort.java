package com.tastyhouse.application.policy.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

public interface PolicyDocumentLoadPort {

    Optional<PolicyDocument> findById(PolicyDocumentId id);

    Optional<PolicyDocument> findCurrentEntityByType(PolicyType type);
}
