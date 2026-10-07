package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PolicyElectronicFinancialTransactionsListQueryUseCase {

    PageResult<PolicyListItemResult> getElectronicFinancialTransactionsList(int page, int size);
}
