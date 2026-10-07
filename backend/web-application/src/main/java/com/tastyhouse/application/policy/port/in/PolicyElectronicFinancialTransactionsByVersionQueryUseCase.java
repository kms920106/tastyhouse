package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;

public interface PolicyElectronicFinancialTransactionsByVersionQueryUseCase {

    PolicyDocumentResult getElectronicFinancialTransactionsByVersion(String version);
}
