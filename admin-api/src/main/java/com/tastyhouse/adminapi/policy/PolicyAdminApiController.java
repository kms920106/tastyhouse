package com.tastyhouse.adminapi.policy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.policy.request.PolicyCreateRequest;
import com.tastyhouse.adminapi.policy.request.PolicyUpdateRequest;
import com.tastyhouse.core.domain.policy.application.PolicyCommandService;
import com.tastyhouse.core.domain.policy.application.dto.command.CreatePolicyCommand;
import com.tastyhouse.core.domain.policy.application.dto.command.UpdatePolicyCommand;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;

@Tag(name = "Policy Admin", description = "약관 및 정책 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/policies")
public class PolicyAdminApiController {

    private final PolicyCommandService policyCommandService;

    @Operation(summary = "약관 생성", description = "새로운 약관을 생성합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createPolicy(@Valid @RequestBody PolicyCreateRequest request) {
        PolicyDocumentId id = policyCommandService.createPolicy(new CreatePolicyCommand(
            request.type(),
            request.version(),
            request.title(),
            request.content(),
            request.mandatory(),
            request.effectiveDate(),
            request.createdBy()
        ));
        return ResponseEntity.ok(ApiResponse.success(id.value()));
    }

    @Operation(summary = "약관 수정", description = "기존 약관을 수정합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePolicy(
        @PathVariable Long id,
        @Valid @RequestBody PolicyUpdateRequest request
    ) {
        policyCommandService.updatePolicy(new PolicyDocumentId(id), new UpdatePolicyCommand(
            request.title(),
            request.content(),
            request.mandatory(),
            request.effectiveDate(),
            request.updatedBy()
        ));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "현재 약관 변경", description = "지정된 약관을 현재 유효한 약관으로 변경합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PatchMapping("/v1/{id}/current")
    public ResponseEntity<ApiResponse<Void>> updateCurrentPolicy(@PathVariable Long id) {
        policyCommandService.activatePolicy(new PolicyDocumentId(id));
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
