package com.tastyhouse.webapi.policy;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.domain.policy.application.PolicyQueryService;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy", description = "약관 및 정책 관리 API")
public class PolicyApiController {

    private final PolicyQueryService policyQueryService;

    @Operation(summary = "최신 이용약관 조회", description = "현재 유효한 최신 이용약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestTermsOfService() {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findCurrentByType(PolicyType.TERMS_OF_SERVICE))));
    }

    @Operation(summary = "최신 개인정보처리방침 조회", description = "현재 유효한 최신 개인정보처리방침을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestPrivacyPolicy() {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findCurrentByType(PolicyType.PRIVACY_POLICY))));
    }

    @Operation(summary = "최신 전자금융거래 약관 조회", description = "현재 유효한 최신 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestElectronicFinancialTransactions() {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findCurrentByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS))));
    }

    @Operation(summary = "최신 만 14세 이상 동의 약관 조회", description = "현재 유효한 최신 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestAgeVerification() {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findCurrentByType(PolicyType.AGE_VERIFICATION))));
    }

    @Operation(summary = "특정 버전 이용약관 조회", description = "지정된 버전의 이용약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getTermsOfServiceByVersion(@PathVariable String version) {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.TERMS_OF_SERVICE, version))));
    }

    @Operation(summary = "특정 버전 개인정보처리방침 조회", description = "지정된 버전의 개인정보처리방침을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getPrivacyPolicyByVersion(@PathVariable String version) {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.PRIVACY_POLICY, version))));
    }

    @Operation(summary = "특정 버전 전자금융거래 약관 조회", description = "지정된 버전의 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getElectronicFinancialTransactionsByVersion(@PathVariable String version) {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, version))));
    }

    @Operation(summary = "특정 버전 만 14세 이상 동의 약관 조회", description = "지정된 버전의 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getAgeVerificationByVersion(@PathVariable String version) {
        return ResponseEntity.ok(CommonResponse.success(toDetailResponse(policyQueryService.findByTypeAndVersion(PolicyType.AGE_VERIFICATION, version))));
    }

    @Operation(summary = "이용약관 목록 조회", description = "모든 버전의 이용약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getTermsOfServiceList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = PageResult.from(policyQueryService.findAllByType(PolicyType.TERMS_OF_SERVICE, pageRequest.page(), pageRequest.size()))
            .map(this::toListItemResponse);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    @Operation(summary = "개인정보처리방침 목록 조회", description = "모든 버전의 개인정보처리방침 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getPrivacyPolicyList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = PageResult.from(policyQueryService.findAllByType(PolicyType.PRIVACY_POLICY, pageRequest.page(), pageRequest.size()))
            .map(this::toListItemResponse);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    @Operation(summary = "전자금융거래 약관 목록 조회", description = "모든 버전의 전자금융거래 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getElectronicFinancialTransactionsList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = PageResult.from(policyQueryService.findAllByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, pageRequest.page(), pageRequest.size()))
            .map(this::toListItemResponse);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    @Operation(summary = "만 14세 이상 동의 약관 목록 조회", description = "모든 버전의 만 14세 이상 동의 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getAgeVerificationList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = PageResult.from(policyQueryService.findAllByType(PolicyType.AGE_VERIFICATION, pageRequest.page(), pageRequest.size()))
            .map(this::toListItemResponse);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    private PolicyDetailResponse toDetailResponse(PolicyDocumentResult result) {
        return PolicyDetailResponse.from(
            result.id(),
            result.type(),
            result.version(),
            result.title(),
            result.content(),
            result.current(),
            result.mandatory(),
            result.effectiveDate(),
            result.createdAt(),
            result.updatedAt()
        );
    }

    private PolicyListItemResponse toListItemResponse(PolicyListItemResult result) {
        return PolicyListItemResponse.from(
            result.id(),
            result.type(),
            result.version(),
            result.title(),
            result.current(),
            result.effectiveDate(),
            result.createdAt()
        );
    }
}
