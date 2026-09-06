package com.tastyhouse.domain.policy.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}
