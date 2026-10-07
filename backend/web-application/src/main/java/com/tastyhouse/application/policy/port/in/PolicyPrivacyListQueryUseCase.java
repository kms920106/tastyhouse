package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PolicyPrivacyListQueryUseCase {

    PageResult<PolicyListItemResult> getPrivacyPolicyList(int page, int size);
}
