package com.tastyhouse.webapi.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.policy.query.PolicyDocumentResult;
import com.tastyhouse.infrastructure.policy.query.PolicyListItemResult;
import com.tastyhouse.infrastructure.policy.query.PolicyQueryDao;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;

/**
 * 약관·정책 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다(쓰기는 admin-api의
 * {@code PolicyCommandService}가 담당). infra read 어댑터({@link PolicyQueryDao})를 주입해 조회하고
 * Response를 조립하며, write 포트는 주입하지 않는다.
 *
 * <p>정책 유형은 약관 종류별 전용 엔드포인트로 고정되어 있어 HTTP 파라미터로 받지 않고 이 서비스가
 * 직접 core enum 상수를 지정한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PolicyQueryService {

    private final PolicyQueryDao policyQueryDao;

    public PolicyDetailResponse getLatestTermsOfService() {
        return getLatestByType(PolicyType.TERMS_OF_SERVICE);
    }

    public PolicyDetailResponse getLatestPrivacyPolicy() {
        return getLatestByType(PolicyType.PRIVACY_POLICY);
    }

    public PolicyDetailResponse getLatestElectronicFinancialTransactions() {
        return getLatestByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS);
    }

    public PolicyDetailResponse getLatestAgeVerification() {
        return getLatestByType(PolicyType.AGE_VERIFICATION);
    }

    public PolicyDetailResponse getTermsOfServiceByVersion(String version) {
        return getByTypeAndVersion(PolicyType.TERMS_OF_SERVICE, version);
    }

    public PolicyDetailResponse getPrivacyPolicyByVersion(String version) {
        return getByTypeAndVersion(PolicyType.PRIVACY_POLICY, version);
    }

    public PolicyDetailResponse getElectronicFinancialTransactionsByVersion(String version) {
        return getByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, version);
    }

    public PolicyDetailResponse getAgeVerificationByVersion(String version) {
        return getByTypeAndVersion(PolicyType.AGE_VERIFICATION, version);
    }

    public PaginationResponse<PolicyListItemResponse> getTermsOfServiceList(int page, int size) {
        return getListByType(PolicyType.TERMS_OF_SERVICE, page, size);
    }

    public PaginationResponse<PolicyListItemResponse> getPrivacyPolicyList(int page, int size) {
        return getListByType(PolicyType.PRIVACY_POLICY, page, size);
    }

    public PaginationResponse<PolicyListItemResponse> getElectronicFinancialTransactionsList(int page, int size) {
        return getListByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, page, size);
    }

    public PaginationResponse<PolicyListItemResponse> getAgeVerificationList(int page, int size) {
        return getListByType(PolicyType.AGE_VERIFICATION, page, size);
    }

    private PolicyDetailResponse getLatestByType(PolicyType type) {
        PolicyDocumentResult result = policyQueryDao.findCurrentByType(type)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_CURRENT_NOT_FOUND));
        return toPolicyDetailResponse(result);
    }

    private PolicyDetailResponse getByTypeAndVersion(PolicyType type, String version) {
        PolicyDocumentResult result = policyQueryDao.findByTypeAndVersion(type, version)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_VERSION_NOT_FOUND));
        return toPolicyDetailResponse(result);
    }

    private PaginationResponse<PolicyListItemResponse> getListByType(PolicyType type, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<PolicyListItemResponse> pageResult = policyQueryDao.findAllByType(type, pageQuery)
            .map(this::toPolicyListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private PolicyDetailResponse toPolicyDetailResponse(PolicyDocumentResult dto) {
        return PolicyDetailResponse.from(
            dto.id(),
            dto.type().name(),
            dto.version(),
            dto.title(),
            dto.content(),
            dto.current(),
            dto.mandatory(),
            dto.effectiveDate(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    private PolicyListItemResponse toPolicyListItemResponse(PolicyListItemResult dto) {
        return PolicyListItemResponse.from(
            dto.id(),
            dto.type().name(),
            dto.version(),
            dto.title(),
            dto.current(),
            dto.effectiveDate(),
            dto.createdAt()
        );
    }
}
