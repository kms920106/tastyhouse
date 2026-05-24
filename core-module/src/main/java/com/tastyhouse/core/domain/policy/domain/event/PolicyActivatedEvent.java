package com.tastyhouse.core.domain.policy.domain.event;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;

import java.time.LocalDateTime;

public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}
