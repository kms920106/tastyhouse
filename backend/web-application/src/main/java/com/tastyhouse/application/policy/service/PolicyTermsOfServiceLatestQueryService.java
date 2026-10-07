package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyTermsOfServiceLatestQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PolicyTermsOfServiceLatestQueryService implements PolicyTermsOfServiceLatestQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyTermsOfServiceLatestQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getLatestTermsOfService() {
        return policyQueryPort.findCurrentByType(PolicyType.TERMS_OF_SERVICE.name())
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.POLICY_CURRENT_NOT_FOUND));
    }
}
