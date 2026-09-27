package com.tastyhouse.application.policy.store;

import java.util.Optional;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentStatePort;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

public class PolicyDocumentStore implements PolicyDocumentRepository {
    private final PolicyDocumentStatePort policyDocumentStatePort;

    public PolicyDocumentStore(PolicyDocumentStatePort policyDocumentStatePort) {
        this.policyDocumentStatePort = policyDocumentStatePort;
    }

    @Override
    public Optional<PolicyDocument> findById(PolicyDocumentId id) {
        return policyDocumentStatePort.findById(id.value()).map(PolicyDocumentStateMapper::toDomain);
    }

    @Override
    public Optional<PolicyDocument> findCurrentEntityByType(PolicyType type) {
        return policyDocumentStatePort.findCurrentEntityByType(type == null ? null : type.name())
            .map(PolicyDocumentStateMapper::toDomain);
    }

    @Override
    public PolicyDocument save(PolicyDocument policyDocument) {
        return PolicyDocumentStateMapper.toDomain(
            policyDocumentStatePort.save(PolicyDocumentStateMapper.toState(policyDocument)));
    }
}
