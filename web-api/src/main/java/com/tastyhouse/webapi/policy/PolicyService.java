package com.tastyhouse.webapi.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.application.PolicyQueryService;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;
import com.tastyhouse.webapi.policy.response.PolicyListPageResult;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyQueryService policyQueryService;

    public PolicyDetailResponse getLatestTermsOfService() {
        return toDetailResponse(policyQueryService.findCurrentByType(PolicyType.TERMS_OF_SERVICE));
    }

    public PolicyDetailResponse getLatestPrivacyPolicy() {
        return toDetailResponse(policyQueryService.findCurrentByType(PolicyType.PRIVACY_POLICY));
    }

    public PolicyDetailResponse getLatestElectronicFinancialTransactions() {
        return toDetailResponse(policyQueryService.findCurrentByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS));
    }

    public PolicyDetailResponse getLatestAgeVerification() {
        return toDetailResponse(policyQueryService.findCurrentByType(PolicyType.AGE_VERIFICATION));
    }

    public PolicyDetailResponse getTermsOfServiceByVersion(String version) {
        return toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.TERMS_OF_SERVICE, version));
    }

    public PolicyDetailResponse getPrivacyPolicyByVersion(String version) {
        return toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.PRIVACY_POLICY, version));
    }

    public PolicyDetailResponse getElectronicFinancialTransactionsByVersion(String version) {
        return toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, version));
    }

    public PolicyDetailResponse getAgeVerificationByVersion(String version) {
        return toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.AGE_VERIFICATION, version));
    }

    public PolicyListPageResult getTermsOfServiceList(int page, int size) {
        return toListPageResult(policyQueryService.findAllByType(PolicyType.TERMS_OF_SERVICE, page, size));
    }

    public PolicyListPageResult getPrivacyPolicyList(int page, int size) {
        return toListPageResult(policyQueryService.findAllByType(PolicyType.PRIVACY_POLICY, page, size));
    }

    public PolicyListPageResult getElectronicFinancialTransactionsList(int page, int size) {
        return toListPageResult(policyQueryService.findAllByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, page, size));
    }

    public PolicyListPageResult getAgeVerificationList(int page, int size) {
        return toListPageResult(policyQueryService.findAllByType(PolicyType.AGE_VERIFICATION, page, size));
    }

    private PolicyListPageResult toListPageResult(PageResult<PolicyListItemResult> pageResult) {
        var mapped = pageResult.map(this::toListItemResponse);
        return PolicyListPageResult.of(mapped.content(), mapped.page(), mapped.size(), mapped.totalElements());
    }

    private PolicyDetailResponse toDetailResponse(PolicyDocumentResult result) {
        return PolicyDetailResponse.from(
            result.id(),
            result.type().name(),
            result.version(),
            result.title(),
            result.content(),
            result.current(),
            result.mandatory(),
            result.effectiveDate(),
            result.createdAt(),
            result.updatedAt()
        );
    }

    private PolicyListItemResponse toListItemResponse(PolicyListItemResult result) {
        return PolicyListItemResponse.from(
            result.id(),
            result.type().name(),
            result.version(),
            result.title(),
            result.current(),
            result.effectiveDate(),
            result.createdAt()
        );
    }
}
