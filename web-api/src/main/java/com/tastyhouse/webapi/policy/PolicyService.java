package com.tastyhouse.webapi.policy;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.policy.PolicyType;
import com.tastyhouse.core.entity.policy.dto.PolicyDocumentDto;
import com.tastyhouse.core.entity.policy.dto.PolicyListItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.PolicyDocumentCoreService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyDocumentCoreService policyDocumentCoreService;

    @Transactional(readOnly = true)
    public PolicyDetailResponse getCurrentByType(PolicyType type) {
        PolicyDocumentDto dto = policyDocumentCoreService.findCurrentByType(type)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_CURRENT_NOT_FOUND));

        return convertToPolicyDetailResponse(dto);
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse getByTypeAndVersion(PolicyType type, String version) {
        PolicyDocumentDto dto = policyDocumentCoreService.findByTypeAndVersion(type, version)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_VERSION_NOT_FOUND));

        return convertToPolicyDetailResponse(dto);
    }

    @Transactional(readOnly = true)
    public PageResult<PolicyListItemResponse> searchAllByType(PolicyType type, PageRequest pageRequest) {
        return policyDocumentCoreService
            .findAllByTypeWithPagination(type, pageRequest.page(), pageRequest.size())
            .map(this::convertToPolicyListItemResponse);
    }

    private PolicyDetailResponse convertToPolicyDetailResponse(PolicyDocumentDto dto) {
        return new PolicyDetailResponse(
            dto.id(), dto.type(), dto.version(), dto.title(), dto.content(),
            dto.current(), dto.mandatory(), dto.effectiveDate(), dto.createdAt(), dto.updatedAt()
        );
    }

    private PolicyListItemResponse convertToPolicyListItemResponse(PolicyListItemDto dto) {
        return new PolicyListItemResponse(
            dto.id(), dto.type(), dto.version(), dto.title(), dto.current(),
            dto.effectiveDate(), dto.createdAt()
        );
    }
}
