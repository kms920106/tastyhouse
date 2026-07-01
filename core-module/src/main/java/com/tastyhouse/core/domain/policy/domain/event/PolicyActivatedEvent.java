package com.tastyhouse.core.domain.policy.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;

public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}
