package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@WebApp
public interface PolicyVersionListQueryUseCase {

    PageResult<PolicyListItemResult> getTermsOfServiceList(int page, int size);

    PageResult<PolicyListItemResult> getPrivacyPolicyList(int page, int size);

    PageResult<PolicyListItemResult> getElectronicFinancialTransactionsList(int page, int size);

    PageResult<PolicyListItemResult> getAgeVerificationList(int page, int size);
}
