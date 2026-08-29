package com.tastyhouse.webapi.policy.application.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;

/**
 * 약관 버전 목록 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PolicyQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface PolicyVersionListQueryUseCase {

    PaginationResponse<PolicyListItemResponse> getTermsOfServiceList(int page, int size);

    PaginationResponse<PolicyListItemResponse> getPrivacyPolicyList(int page, int size);

    PaginationResponse<PolicyListItemResponse> getElectronicFinancialTransactionsList(int page, int size);

    PaginationResponse<PolicyListItemResponse> getAgeVerificationList(int page, int size);
}
