package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyTermsOfServiceListQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class PolicyTermsOfServiceListQueryService implements PolicyTermsOfServiceListQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyTermsOfServiceListQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PageResult<PolicyListItemResult> getTermsOfServiceList(int page, int size) {
        return policyQueryPort.findAllByType(PolicyType.TERMS_OF_SERVICE.name(), PageQuery.of(page, size));
    }
}
