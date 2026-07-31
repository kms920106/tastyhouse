package com.tastyhouse.domain.policy.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.domain.model.PolicyType;
import com.tastyhouse.domain.policy.domain.vo.PolicyDocumentId;

public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}
