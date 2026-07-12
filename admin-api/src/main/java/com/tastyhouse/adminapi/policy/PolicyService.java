package com.tastyhouse.adminapi.policy;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import com.tastyhouse.core.domain.policy.application.PolicyCommandService;
import com.tastyhouse.core.domain.policy.application.dto.command.PolicyCreateCommand;
import com.tastyhouse.core.domain.policy.application.dto.command.PolicyUpdateCommand;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyCommandService policyCommandService;

    public Long createPolicy(
        String type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        PolicyDocumentId id = policyCommandService.createPolicy(
            PolicyCreateCommand.of(PolicyType.from(type), version, title, content, mandatory, effectiveDate, createdBy));
        return id.value();
    }

    public void updatePolicy(
        Long id,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String updatedBy
    ) {
        policyCommandService.updatePolicy(PolicyDocumentId.of(id),
            PolicyUpdateCommand.of(title, content, mandatory, effectiveDate, updatedBy));
    }

    public void activateCurrentPolicy(Long id) {
        policyCommandService.activatePolicy(PolicyDocumentId.of(id));
    }
}
