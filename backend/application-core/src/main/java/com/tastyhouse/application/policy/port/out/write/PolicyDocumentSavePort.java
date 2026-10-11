package com.tastyhouse.application.policy.port.out.write;

import com.tastyhouse.domain.policy.model.PolicyDocument;

public interface PolicyDocumentSavePort {

    PolicyDocument save(PolicyDocument policyDocument);
}
