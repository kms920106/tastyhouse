package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PolicyAgeVerificationListQueryUseCase {

    PageResult<PolicyListItemResult> getAgeVerificationList(int page, int size);
}
