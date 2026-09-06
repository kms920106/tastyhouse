package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.shared.marker.WebApp;

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
