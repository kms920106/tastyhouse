package com.tastyhouse.adminapi.coupon.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.request.CouponCreateRequest;
import com.tastyhouse.adminapi.coupon.adapter.in.web.request.CouponIssueRequest;
import com.tastyhouse.adminapi.coupon.adapter.in.web.request.CouponSearchRequest;
import com.tastyhouse.adminapi.coupon.adapter.in.web.request.CouponUpdateRequest;
import com.tastyhouse.adminapplication.coupon.response.CouponDetailResponse;
import com.tastyhouse.adminapplication.coupon.response.CouponListItemResponse;
import com.tastyhouse.adminapplication.coupon.response.MemberCouponItemResponse;
import com.tastyhouse.adminapplication.coupon.port.in.CouponCommandUseCase;
import com.tastyhouse.adminapplication.coupon.port.in.CouponCreateCommand;
import com.tastyhouse.adminapplication.coupon.port.in.CouponDeleteCommand;
import com.tastyhouse.adminapplication.coupon.port.in.CouponIssueCommand;
import com.tastyhouse.adminapplication.coupon.port.in.CouponUpdateCommand;
import com.tastyhouse.adminapplication.coupon.port.in.CouponQueryUseCase;

@Tag(name = "Coupon Admin", description = "쿠폰 관리자 API")
@RestController
@RequestMapping("/api/coupons")
public class CouponApiController {

    private final CouponCommandUseCase couponCommandUseCase;
    private final CouponQueryUseCase couponQueryUseCase;

    public CouponApiController(CouponCommandUseCase couponCommandUseCase, CouponQueryUseCase couponQueryUseCase) {
        this.couponCommandUseCase = couponCommandUseCase;
        this.couponQueryUseCase = couponQueryUseCase;
    }

    @Operation(summary = "쿠폰 목록 조회", description = "쿠폰 목록을 페이징 조회합니다. (삭제된 쿠폰 제외) discountType 미지정 시 전체 유형, name은 부분 일치 검색, visible은 null=전체/true=노출/false=비노출")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<CouponListItemResponse>>> getCoupons(
        @Valid @ModelAttribute CouponSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<CouponListItemResponse> pageResponse = couponQueryUseCase.getCoupons(search.name(), search.discountType(), search.visible(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "쿠폰 등록", description = "새로운 쿠폰을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        CouponCreateCommand command = request.toCommand();
        Long id = couponCommandUseCase.createCoupon(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "쿠폰 상세 조회", description = "쿠폰 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCoupon(@PathVariable Long id) {
        CouponDetailResponse response = couponQueryUseCase.getCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "쿠폰 수정", description = "기존 쿠폰을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCoupon(
        @PathVariable Long id,
        @Valid @RequestBody CouponUpdateRequest request
    ) {
        CouponUpdateCommand command = request.toCommand(id);
        couponCommandUseCase.updateCoupon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "쿠폰 삭제", description = "기존 쿠폰을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        CouponDeleteCommand command = CouponDeleteCommand.of(id);
        couponCommandUseCase.deleteCoupon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "쿠폰 회원 발급", description = "특정 회원에게 쿠폰을 수동 발급합니다. 만료 일시는 쿠폰의 사용 종료 일시로 설정됩니다.")
    @PostMapping("/v1/{id}/issues")
    public ResponseEntity<ApiResponse<Long>> issueCoupon(
        @PathVariable Long id,
        @Valid @RequestBody CouponIssueRequest request
    ) {
        CouponIssueCommand command = request.toCommand(id);
        Long memberCouponId = couponCommandUseCase.issueCoupon(command);
        return ResponseEntity.ok(ApiResponse.success(memberCouponId));
    }

    @Operation(summary = "쿠폰 발급 현황 조회", description = "특정 쿠폰의 회원 발급 현황을 페이징 조회합니다.")
    @GetMapping("/v1/{id}/issues")
    public ResponseEntity<ApiResponse<List<MemberCouponItemResponse>>> getIssuedCoupons(
        @PathVariable Long id,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<MemberCouponItemResponse> pageResponse = couponQueryUseCase.getIssuedCoupons(id, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }
}
