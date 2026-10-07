package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyElectronicFinancialTransactionsByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PolicyElectronicFinancialTransactionsByVersionQueryService implements PolicyElectronicFinancialTransactionsByVersionQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyElectronicFinancialTransactionsByVersionQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getElectronicFinancialTransactionsByVersion(String version) {
        return policyQueryPort.findByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS.name(), version)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.POLICY_VERSION_NOT_FOUND));
    }
}
