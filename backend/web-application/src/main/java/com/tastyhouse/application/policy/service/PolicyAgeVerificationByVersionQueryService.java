package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyAgeVerificationByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PolicyAgeVerificationByVersionQueryService implements PolicyAgeVerificationByVersionQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyAgeVerificationByVersionQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getAgeVerificationByVersion(String version) {
        return policyQueryPort.findByTypeAndVersion(PolicyType.AGE_VERIFICATION.name(), version)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.POLICY_VERSION_NOT_FOUND));
    }
}
