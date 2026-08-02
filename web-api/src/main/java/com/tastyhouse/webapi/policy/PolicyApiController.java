package com.tastyhouse.webapi.policy;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.webapi.policy.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.response.PolicyListItemResponse;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy", description = "약관 및 정책 관리 API")
public class PolicyApiController {

    private final PolicyQueryService policyQueryService;

    @Operation(summary = "최신 이용약관 조회", description = "현재 유효한 최신 이용약관을 조회합니다.")
    @GetMapping("/v1/terms-of-service/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestTermsOfService() {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getLatestTermsOfService()));
    }

    @Operation(summary = "최신 개인정보처리방침 조회", description = "현재 유효한 최신 개인정보처리방침을 조회합니다.")
    @GetMapping("/v1/privacy-policy/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestPrivacyPolicy() {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getLatestPrivacyPolicy()));
    }

    @Operation(summary = "최신 전자금융거래 약관 조회", description = "현재 유효한 최신 전자금융거래 약관을 조회합니다.")
    @GetMapping("/v1/electronic-financial-transactions/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestElectronicFinancialTransactions() {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getLatestElectronicFinancialTransactions()));
    }

    @Operation(summary = "최신 만 14세 이상 동의 약관 조회", description = "현재 유효한 최신 만 14세 이상 동의 약관을 조회합니다.")
    @GetMapping("/v1/age-verification/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestAgeVerification() {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getLatestAgeVerification()));
    }

    @Operation(summary = "특정 버전 이용약관 조회", description = "지정된 버전의 이용약관을 조회합니다.")
    @GetMapping("/v1/terms-of-service/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getTermsOfServiceByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getTermsOfServiceByVersion(version)));
    }

    @Operation(summary = "특정 버전 개인정보처리방침 조회", description = "지정된 버전의 개인정보처리방침을 조회합니다.")
    @GetMapping("/v1/privacy-policy/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getPrivacyPolicyByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getPrivacyPolicyByVersion(version)));
    }

    @Operation(summary = "특정 버전 전자금융거래 약관 조회", description = "지정된 버전의 전자금융거래 약관을 조회합니다.")
    @GetMapping("/v1/electronic-financial-transactions/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getElectronicFinancialTransactionsByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getElectronicFinancialTransactionsByVersion(version)));
    }

    @Operation(summary = "특정 버전 만 14세 이상 동의 약관 조회", description = "지정된 버전의 만 14세 이상 동의 약관을 조회합니다.")
    @GetMapping("/v1/age-verification/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getAgeVerificationByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(policyQueryService.getAgeVerificationByVersion(version)));
    }

    @Operation(summary = "이용약관 목록 조회", description = "모든 버전의 이용약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/terms-of-service")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getTermsOfServiceList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyQueryService.getTermsOfServiceList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "개인정보처리방침 목록 조회", description = "모든 버전의 개인정보처리방침 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/privacy-policy")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getPrivacyPolicyList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyQueryService.getPrivacyPolicyList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "전자금융거래 약관 목록 조회", description = "모든 버전의 전자금융거래 약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/electronic-financial-transactions")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getElectronicFinancialTransactionsList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyQueryService.getElectronicFinancialTransactionsList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "만 14세 이상 동의 약관 목록 조회", description = "모든 버전의 만 14세 이상 동의 약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/age-verification")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getAgeVerificationList(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = policyQueryService.getAgeVerificationList(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }
}
