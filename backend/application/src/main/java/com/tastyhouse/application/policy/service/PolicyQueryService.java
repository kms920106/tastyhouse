package com.tastyhouse.application.policy.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.policy.port.in.PolicyDetailQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyVersionListQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class PolicyQueryService implements PolicyDetailQueryUseCase, PolicyVersionListQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PolicyDocumentResult getLatestTermsOfService() {
        return getLatestByType(PolicyType.TERMS_OF_SERVICE);
    }

    @Override
    public PolicyDocumentResult getLatestPrivacyPolicy() {
        return getLatestByType(PolicyType.PRIVACY_POLICY);
    }

    @Override
    public PolicyDocumentResult getLatestElectronicFinancialTransactions() {
        return getLatestByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS);
    }

    @Override
    public PolicyDocumentResult getLatestAgeVerification() {
        return getLatestByType(PolicyType.AGE_VERIFICATION);
    }

    @Override
    public PolicyDocumentResult getTermsOfServiceByVersion(String version) {
        return getByTypeAndVersion(PolicyType.TERMS_OF_SERVICE, version);
    }

    @Override
    public PolicyDocumentResult getPrivacyPolicyByVersion(String version) {
        return getByTypeAndVersion(PolicyType.PRIVACY_POLICY, version);
    }

    @Override
    public PolicyDocumentResult getElectronicFinancialTransactionsByVersion(String version) {
        return getByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, version);
    }

    @Override
    public PolicyDocumentResult getAgeVerificationByVersion(String version) {
        return getByTypeAndVersion(PolicyType.AGE_VERIFICATION, version);
    }

    @Override
    public PageResult<PolicyListItemResult> getTermsOfServiceList(int page, int size) {
        return getListByType(PolicyType.TERMS_OF_SERVICE, page, size);
    }

    @Override
    public PageResult<PolicyListItemResult> getPrivacyPolicyList(int page, int size) {
        return getListByType(PolicyType.PRIVACY_POLICY, page, size);
    }

    @Override
    public PageResult<PolicyListItemResult> getElectronicFinancialTransactionsList(int page, int size) {
        return getListByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, page, size);
    }

    @Override
    public PageResult<PolicyListItemResult> getAgeVerificationList(int page, int size) {
        return getListByType(PolicyType.AGE_VERIFICATION, page, size);
    }

    private PolicyDocumentResult getLatestByType(PolicyType type) {
        return policyQueryPort.findCurrentByType(type)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POLICY_CURRENT_NOT_FOUND));
    }

    private PolicyDocumentResult getByTypeAndVersion(PolicyType type, String version) {
        return policyQueryPort.findByTypeAndVersion(type, version)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POLICY_VERSION_NOT_FOUND));
    }

    private PageResult<PolicyListItemResult> getListByType(PolicyType type, int page, int size) {
        return policyQueryPort.findAllByType(type, PageQuery.of(page, size));
    }
}
