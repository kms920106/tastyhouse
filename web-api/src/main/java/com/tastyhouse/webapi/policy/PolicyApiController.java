package com.tastyhouse.webapi.policy;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.entity.policy.PolicyType;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
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

    private final PolicyService policyService;

    @Operation(summary = "최신 이용약관 조회", description = "현재 유효한 최신 이용약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestTermsOfService() {
        PolicyDetailResponse response = policyService.getCurrentByType(PolicyType.TERMS_OF_SERVICE);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "최신 개인정보처리방침 조회", description = "현재 유효한 최신 개인정보처리방침을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestPrivacyPolicy() {
        PolicyDetailResponse response = policyService.getCurrentByType(PolicyType.PRIVACY_POLICY);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "최신 전자금융거래 약관 조회", description = "현재 유효한 최신 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestElectronicFinancialTransactions() {
        PolicyDetailResponse response = policyService.getCurrentByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "최신 만 14세 이상 동의 약관 조회", description = "현재 유효한 최신 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification/latest")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getLatestAgeVerification() {
        PolicyDetailResponse response = policyService.getCurrentByType(PolicyType.AGE_VERIFICATION);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "특정 버전 이용약관 조회", description = "지정된 버전의 이용약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getTermsOfServiceByVersion(@PathVariable String version) {
        PolicyDetailResponse response = policyService.getByTypeAndVersion(PolicyType.TERMS_OF_SERVICE, version);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "특정 버전 개인정보처리방침 조회", description = "지정된 버전의 개인정보처리방침을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getPrivacyPolicyByVersion(@PathVariable String version) {
        PolicyDetailResponse response = policyService.getByTypeAndVersion(PolicyType.PRIVACY_POLICY, version);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "특정 버전 전자금융거래 약관 조회", description = "지정된 버전의 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getElectronicFinancialTransactionsByVersion(@PathVariable String version) {
        PolicyDetailResponse response = policyService.getByTypeAndVersion(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, version);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "특정 버전 만 14세 이상 동의 약관 조회", description = "지정된 버전의 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification/version/{version}")
    public ResponseEntity<CommonResponse<PolicyDetailResponse>> getAgeVerificationByVersion(@PathVariable String version) {
        PolicyDetailResponse response = policyService.getByTypeAndVersion(PolicyType.AGE_VERIFICATION, version);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


    @Operation(summary = "이용약관 목록 조회", description = "모든 버전의 이용약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/terms-of-service")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getTermsOfServiceList(
        @Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = policyService.searchAllByType(PolicyType.TERMS_OF_SERVICE, pageRequest);
        CommonResponse<List<PolicyListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "개인정보처리방침 목록 조회", description = "모든 버전의 개인정보처리방침 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/privacy-policy")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getPrivacyPolicyList(
        @Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = policyService.searchAllByType(PolicyType.PRIVACY_POLICY, pageRequest);
        CommonResponse<List<PolicyListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전자금융거래 약관 목록 조회", description = "모든 버전의 전자금융거래 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getElectronicFinancialTransactionsList(
        @Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = policyService.searchAllByType(PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, pageRequest);
        CommonResponse<List<PolicyListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "만 14세 이상 동의 약관 목록 조회", description = "모든 버전의 만 14세 이상 동의 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/age-verification")
    public ResponseEntity<CommonResponse<List<PolicyListItemResponse>>> getAgeVerificationList(
        @Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<PolicyListItemResponse> pageResult = policyService.searchAllByType(PolicyType.AGE_VERIFICATION, pageRequest);
        CommonResponse<List<PolicyListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }
}
