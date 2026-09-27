package com.tastyhouse.application.policy.port.out.write;

import java.util.Optional;

public interface PolicyDocumentStatePort {
    Optional<PolicyDocumentState> findById(Long id);

    Optional<PolicyDocumentState> findCurrentEntityByType(String type);

    PolicyDocumentState save(PolicyDocumentState state);
}
