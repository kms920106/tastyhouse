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
        PolicyCreateCommand command = PolicyCreateCommand.of(
            PolicyType.from(type), version, title, content, mandatory, effectiveDate, createdBy);
        PolicyDocumentId id = policyCommandService.createPolicy(command);
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
        PolicyUpdateCommand command = PolicyUpdateCommand.of(title, content, mandatory, effectiveDate, updatedBy);
        policyCommandService.updatePolicy(PolicyDocumentId.of(id), command);
    }

    public void activateCurrentPolicy(Long id) {
        policyCommandService.activatePolicy(PolicyDocumentId.of(id));
    }
}
