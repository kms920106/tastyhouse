package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyPrivacyByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PolicyPrivacyByVersionQueryService implements PolicyPrivacyByVersionQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyPrivacyByVersionQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getPrivacyPolicyByVersion(String version) {
        return policyQueryPort.findByTypeAndVersion(PolicyType.PRIVACY_POLICY.name(), version)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.POLICY_VERSION_NOT_FOUND));
    }
}
