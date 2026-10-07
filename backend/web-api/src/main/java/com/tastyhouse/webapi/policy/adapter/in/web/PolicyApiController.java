package com.tastyhouse.webapi.policy.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.policy.port.in.PolicyAgeVerificationByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyAgeVerificationLatestQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyAgeVerificationListQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyElectronicFinancialTransactionsByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyElectronicFinancialTransactionsLatestQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyElectronicFinancialTransactionsListQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyPrivacyByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyPrivacyLatestQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyPrivacyListQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyTermsOfServiceByVersionQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyTermsOfServiceLatestQueryUseCase;
import com.tastyhouse.application.policy.port.in.PolicyTermsOfServiceListQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.policy.adapter.in.web.response.PolicyDetailResponse;
import com.tastyhouse.webapi.policy.adapter.in.web.response.PolicyListItemResponse;

@RestController
@RequestMapping("/api/policies")
@Tag(name = "Policy", description = "약관 및 정책 관리 API")
class PolicyApiController {

    private final PolicyTermsOfServiceLatestQueryUseCase policyTermsOfServiceLatestQueryUseCase;
    private final PolicyTermsOfServiceByVersionQueryUseCase policyTermsOfServiceByVersionQueryUseCase;
    private final PolicyTermsOfServiceListQueryUseCase policyTermsOfServiceListQueryUseCase;
    private final PolicyPrivacyLatestQueryUseCase policyPrivacyLatestQueryUseCase;
    private final PolicyPrivacyByVersionQueryUseCase policyPrivacyByVersionQueryUseCase;
    private final PolicyPrivacyListQueryUseCase policyPrivacyListQueryUseCase;
    private final PolicyElectronicFinancialTransactionsLatestQueryUseCase policyElectronicFinancialTransactionsLatestQueryUseCase;
    private final PolicyElectronicFinancialTransactionsByVersionQueryUseCase policyElectronicFinancialTransactionsByVersionQueryUseCase;
    private final PolicyElectronicFinancialTransactionsListQueryUseCase policyElectronicFinancialTransactionsListQueryUseCase;
    private final PolicyAgeVerificationLatestQueryUseCase policyAgeVerificationLatestQueryUseCase;
    private final PolicyAgeVerificationByVersionQueryUseCase policyAgeVerificationByVersionQueryUseCase;
    private final PolicyAgeVerificationListQueryUseCase policyAgeVerificationListQueryUseCase;

    public PolicyApiController(
        PolicyTermsOfServiceLatestQueryUseCase policyTermsOfServiceLatestQueryUseCase,
        PolicyTermsOfServiceByVersionQueryUseCase policyTermsOfServiceByVersionQueryUseCase,
        PolicyTermsOfServiceListQueryUseCase policyTermsOfServiceListQueryUseCase,
        PolicyPrivacyLatestQueryUseCase policyPrivacyLatestQueryUseCase,
        PolicyPrivacyByVersionQueryUseCase policyPrivacyByVersionQueryUseCase,
        PolicyPrivacyListQueryUseCase policyPrivacyListQueryUseCase,
        PolicyElectronicFinancialTransactionsLatestQueryUseCase policyElectronicFinancialTransactionsLatestQueryUseCase,
        PolicyElectronicFinancialTransactionsByVersionQueryUseCase policyElectronicFinancialTransactionsByVersionQueryUseCase,
        PolicyElectronicFinancialTransactionsListQueryUseCase policyElectronicFinancialTransactionsListQueryUseCase,
        PolicyAgeVerificationLatestQueryUseCase policyAgeVerificationLatestQueryUseCase,
        PolicyAgeVerificationByVersionQueryUseCase policyAgeVerificationByVersionQueryUseCase,
        PolicyAgeVerificationListQueryUseCase policyAgeVerificationListQueryUseCase
    ) {
        this.policyTermsOfServiceLatestQueryUseCase = policyTermsOfServiceLatestQueryUseCase;
        this.policyTermsOfServiceByVersionQueryUseCase = policyTermsOfServiceByVersionQueryUseCase;
        this.policyTermsOfServiceListQueryUseCase = policyTermsOfServiceListQueryUseCase;
        this.policyPrivacyLatestQueryUseCase = policyPrivacyLatestQueryUseCase;
        this.policyPrivacyByVersionQueryUseCase = policyPrivacyByVersionQueryUseCase;
        this.policyPrivacyListQueryUseCase = policyPrivacyListQueryUseCase;
        this.policyElectronicFinancialTransactionsLatestQueryUseCase = policyElectronicFinancialTransactionsLatestQueryUseCase;
        this.policyElectronicFinancialTransactionsByVersionQueryUseCase = policyElectronicFinancialTransactionsByVersionQueryUseCase;
        this.policyElectronicFinancialTransactionsListQueryUseCase = policyElectronicFinancialTransactionsListQueryUseCase;
        this.policyAgeVerificationLatestQueryUseCase = policyAgeVerificationLatestQueryUseCase;
        this.policyAgeVerificationByVersionQueryUseCase = policyAgeVerificationByVersionQueryUseCase;
        this.policyAgeVerificationListQueryUseCase = policyAgeVerificationListQueryUseCase;
    }

    @Operation(summary = "최신 이용약관 조회", description = "현재 유효한 최신 이용약관을 조회합니다.")
    @GetMapping("/v1/terms-of-service/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestTermsOfService() {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyTermsOfServiceLatestQueryUseCase.getLatestTermsOfService())));
    }

    @Operation(summary = "최신 개인정보처리방침 조회", description = "현재 유효한 최신 개인정보처리방침을 조회합니다.")
    @GetMapping("/v1/privacy-policy/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestPrivacyPolicy() {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyPrivacyLatestQueryUseCase.getLatestPrivacyPolicy())));
    }

    @Operation(summary = "최신 전자금융거래 약관 조회", description = "현재 유효한 최신 전자금융거래 약관을 조회합니다.")
    @GetMapping("/v1/electronic-financial-transactions/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestElectronicFinancialTransactions() {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyElectronicFinancialTransactionsLatestQueryUseCase.getLatestElectronicFinancialTransactions())));
    }

    @Operation(summary = "최신 만 14세 이상 동의 약관 조회", description = "현재 유효한 최신 만 14세 이상 동의 약관을 조회합니다.")
    @GetMapping("/v1/age-verification/latest")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getLatestAgeVerification() {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyAgeVerificationLatestQueryUseCase.getLatestAgeVerification())));
    }

    @Operation(summary = "특정 버전 이용약관 조회", description = "지정된 버전의 이용약관을 조회합니다.")
    @GetMapping("/v1/terms-of-service/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getTermsOfServiceByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyTermsOfServiceByVersionQueryUseCase.getTermsOfServiceByVersion(version))));
    }

    @Operation(summary = "특정 버전 개인정보처리방침 조회", description = "지정된 버전의 개인정보처리방침을 조회합니다.")
    @GetMapping("/v1/privacy-policy/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getPrivacyPolicyByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyPrivacyByVersionQueryUseCase.getPrivacyPolicyByVersion(version))));
    }

    @Operation(summary = "특정 버전 전자금융거래 약관 조회", description = "지정된 버전의 전자금융거래 약관을 조회합니다.")
    @GetMapping("/v1/electronic-financial-transactions/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getElectronicFinancialTransactionsByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyElectronicFinancialTransactionsByVersionQueryUseCase.getElectronicFinancialTransactionsByVersion(version))));
    }

    @Operation(summary = "특정 버전 만 14세 이상 동의 약관 조회", description = "지정된 버전의 만 14세 이상 동의 약관을 조회합니다.")
    @GetMapping("/v1/age-verification/version/{version}")
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getAgeVerificationByVersion(@PathVariable String version) {
        return ResponseEntity.ok(ApiResponse.success(PolicyDetailResponse.from(policyAgeVerificationByVersionQueryUseCase.getAgeVerificationByVersion(version))));
    }

    @Operation(summary = "이용약관 목록 조회", description = "모든 버전의 이용약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/terms-of-service")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getTermsOfServiceList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<PolicyListItemResponse> pageResult = PaginationResponse.from(
            policyTermsOfServiceListQueryUseCase.getTermsOfServiceList(pageRequest.page(), pageRequest.size())
                .map(PolicyListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "개인정보처리방침 목록 조회", description = "모든 버전의 개인정보처리방침 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/privacy-policy")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getPrivacyPolicyList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<PolicyListItemResponse> pageResult = PaginationResponse.from(
            policyPrivacyListQueryUseCase.getPrivacyPolicyList(pageRequest.page(), pageRequest.size())
                .map(PolicyListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "전자금융거래 약관 목록 조회", description = "모든 버전의 전자금융거래 약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/electronic-financial-transactions")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getElectronicFinancialTransactionsList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<PolicyListItemResponse> pageResult = PaginationResponse.from(
            policyElectronicFinancialTransactionsListQueryUseCase.getElectronicFinancialTransactionsList(pageRequest.page(), pageRequest.size())
                .map(PolicyListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }

    @Operation(summary = "만 14세 이상 동의 약관 목록 조회", description = "모든 버전의 만 14세 이상 동의 약관 목록을 조회합니다. (관리자용)")
    @GetMapping("/v1/age-verification")
    public ResponseEntity<ApiResponse<List<PolicyListItemResponse>>> getAgeVerificationList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<PolicyListItemResponse> pageResult = PaginationResponse.from(
            policyAgeVerificationListQueryUseCase.getAgeVerificationList(pageRequest.page(), pageRequest.size())
                .map(PolicyListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()));
    }
}
