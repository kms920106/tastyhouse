package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyPrivacyListQueryUseCase;
import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class PolicyPrivacyListQueryService implements PolicyPrivacyListQueryUseCase {

    private final PolicyQueryPort policyQueryPort;

    public PolicyPrivacyListQueryService(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    @Override
    public PageResult<PolicyListItemResult> getPrivacyPolicyList(int page, int size) {
        return policyQueryPort.findAllByType(PolicyType.PRIVACY_POLICY.name(), PageQuery.of(page, size));
    }
}
