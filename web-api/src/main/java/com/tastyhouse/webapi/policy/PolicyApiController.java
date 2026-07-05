package com.tastyhouse.webapi.policy;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy", description = "약관 및 정책 관리 API")
public class PolicyApiController {

    private final PolicyService policyService;

    @Operation(summary = "최신 이용약관 조회", description = "현재 유효한 최신 이용약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/terms-of-service/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestTermsOfService() {
        return ResponseEntity.ok(ApiResponse.success(policyService.getLatestTermsOfService()));
    }

    @Operation(summary = "최신 개인정보처리방침 조회", description = "현재 유효한 최신 개인정보처리방침을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/privacy-policy/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestPrivacyPolicy() {
        return ResponseEntity.ok(ApiResponse.success(policyService.getLatestPrivacyPolicy()));
    }

    @Operation(summary = "최신 전자금융거래 약관 조회", description = "현재 유효한 최신 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestElectronicFinancialTransactions() {
        return ResponseEntity.ok(ApiResponse.success(policyService.getLatestElectronicFinancialTransactions()));
    }

    @Operation(summary = "최신 만 14세 이상 동의 약관 조회", description = "현재 유효한 최신 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/age-verification/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestAgeVerification() {
        return ResponseEntity.ok(ApiResponse.success(policyService.getLatestAgeVerification()));
    }

    @Operation(summary = "특정 버전 이용약관 조회", description = "지정된 버전의 이용약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/terms-of-service/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getTermsOfServiceByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyService.getTermsOfServiceByVersion(version)));
    }

    @Operation(summary = "특정 버전 개인정보처리방침 조회", description = "지정된 버전의 개인정보처리방침을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/privacy-policy/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getPrivacyPolicyByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyService.getPrivacyPolicyByVersion(version)));
    }

    @Operation(summary = "특정 버전 전자금융거래 약관 조회", description = "지정된 버전의 전자금융거래 약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getElectronicFinancialTransactionsByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyService.getElectronicFinancialTransactionsByVersion(version)));
    }

    @Operation(summary = "특정 버전 만 14세 이상 동의 약관 조회", description = "지정된 버전의 만 14세 이상 동의 약관을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/age-verification/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getAgeVerificationByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyService.getAgeVerificationByVersion(version)));
    }

    @Operation(summary = "이용약관 목록 조회", description = "모든 버전의 이용약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/terms-of-service")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getTermsOfServiceList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyService.getTermsOfServiceList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "개인정보처리방침 목록 조회", description = "모든 버전의 개인정보처리방침 목록을 조회합니다. (관리자용)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/privacy-policy")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getPrivacyPolicyList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyService.getPrivacyPolicyList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "전자금융거래 약관 목록 조회", description = "모든 버전의 전자금융거래 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/electronic-financial-transactions")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getElectronicFinancialTransactionsList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyService.getElectronicFinancialTransactionsList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "만 14세 이상 동의 약관 목록 조회", description = "모든 버전의 만 14세 이상 동의 약관 목록을 조회합니다. (관리자용)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/age-verification")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getAgeVerificationList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyService.getAgeVerificationList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }
}
