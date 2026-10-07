package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyAgeVerificationLatestQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PolicyAgeVerificationLatestQueryService implements PolicyAgeVerificationLatestQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyAgeVerificationLatestQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getLatestAgeVerification() {
        return policyQueryPort.findCurrentByType(PolicyType.AGE_VERIFICATION.name())
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.POLICY_CURRENT_NOT_FOUND));
    }
}
