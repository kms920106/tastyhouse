package com.tastyhouse.adminapi.policy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.policy.request.PolicyCreateRequest;
import com.tastyhouse.adminapi.policy.request.PolicyUpdateRequest;

@Tag(name = "Policy Admin", description = "약관 및 정책 관리자 API")
@RestController
@RequestMapping("/api/policies")
public class PolicyAdminApiController {

    private final PolicyCommandService policyCommandService;

    public PolicyAdminApiController(PolicyCommandService policyCommandService) {
        this.policyCommandService = policyCommandService;
    }

    @Operation(summary = "약관 생성", description = "새로운 약관을 생성합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createPolicy(@Valid @RequestBody PolicyCreateRequest request) {
        Long id = policyCommandService.createPolicy(
            request.type(),
            request.version(),
            request.title(),
            request.content(),
            request.mandatory(),
            request.effectiveDate(),
            request.createdBy()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "약관 수정", description = "기존 약관을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePolicy(
        @PathVariable Long id,
        @Valid @RequestBody PolicyUpdateRequest request
    ) {
        policyCommandService.updatePolicy(
            id,
            request.title(),
            request.content(),
            request.mandatory(),
            request.effectiveDate(),
            request.updatedBy()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "현재 약관 변경", description = "지정된 약관을 현재 유효한 약관으로 변경합니다.")
    @PatchMapping("/v1/{id}/current")
    public ResponseEntity<ApiResponse<Void>> updateCurrentPolicy(@PathVariable Long id) {
        policyCommandService.activateCurrentPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
