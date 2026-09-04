package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 약관 본문 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PolicyQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface PolicyDetailQueryUseCase {

    PolicyDocumentResult getLatestTermsOfService();

    PolicyDocumentResult getLatestPrivacyPolicy();

    PolicyDocumentResult getLatestElectronicFinancialTransactions();

    PolicyDocumentResult getLatestAgeVerification();

    PolicyDocumentResult getTermsOfServiceByVersion(String version);

    PolicyDocumentResult getPrivacyPolicyByVersion(String version);

    PolicyDocumentResult getElectronicFinancialTransactionsByVersion(String version);

    PolicyDocumentResult getAgeVerificationByVersion(String version);
}
