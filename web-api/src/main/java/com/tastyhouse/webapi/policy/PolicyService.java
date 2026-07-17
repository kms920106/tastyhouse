package com.tastyhouse.webapi.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.application.PolicyQueryService;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;

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

    public PaginationResponse<PolicyListItemResponse> getTermsOfServiceList(int page, int size) {
        return toPageResponse(policyQueryService.findAllByType(PolicyType.TERMS_OF_SERVICE, page, size));
    }

    public PaginationResponse<PolicyListItemResponse> getPrivacyPolicyList(int page, int size) {
        return toPageResponse(policyQueryService.findAllByType(PolicyType.PRIVACY_POLICY, page, size));
    }

    public PaginationResponse<PolicyListItemResponse> getElectronicFinancialTransactionsList(int page, int size) {
        return toPageResponse(policyQueryService.findAllByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, page, size));
    }

    public PaginationResponse<PolicyListItemResponse> getAgeVerificationList(int page, int size) {
        return toPageResponse(policyQueryService.findAllByType(PolicyType.AGE_VERIFICATION, page, size));
    }

    private PaginationResponse<PolicyListItemResponse> toPageResponse(PageResult<PolicyListItemResult> pageResult) {
        return PaginationResponse.from(pageResult.map(this::toListItemResponse));
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
