package com.tastyhouse.webapplication.policy.service;

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
import com.tastyhouse.webapplication.policy.port.in.PolicyDetailQueryUseCase;
import com.tastyhouse.webapplication.policy.port.in.PolicyVersionListQueryUseCase;

/**
 * 약관·정책 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다(쓰기는 admin-api의
 * {@code PolicyCommandService}가 담당). 읽기 포트({@link PolicyQueryPort})를 주입해 읽기 계약을
 * 돌려주며, write 포트는 주입하지 않는다.
 *
 * <p>정책 유형은 약관 종류별 전용 엔드포인트로 고정되어 있어 HTTP 파라미터로 받지 않고 이 서비스가
 * 직접 core enum 상수를 지정한다.
 */
@Service
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
