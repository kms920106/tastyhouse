package com.tastyhouse.adminapi.coupon;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.coupon.request.CouponCreateRequest;
import com.tastyhouse.adminapi.coupon.request.CouponIssueRequest;
import com.tastyhouse.adminapi.coupon.request.CouponSearchRequest;
import com.tastyhouse.adminapi.coupon.request.CouponUpdateRequest;
import com.tastyhouse.adminapi.coupon.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.response.MemberCouponItemResponse;

@Tag(name = "Coupon Admin", description = "쿠폰 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponApiController {

    private final CouponCommandService couponCommandService;
    private final CouponQueryService couponQueryService;

    @Operation(summary = "쿠폰 목록 조회", description = "쿠폰 목록을 페이징 조회합니다. (삭제된 쿠폰 제외) discountType 미지정 시 전체 유형, name은 부분 일치 검색, visible은 null=전체/true=노출/false=비노출")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<CouponListItemResponse>>> getCoupons(
        @Valid @ModelAttribute CouponSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<CouponListItemResponse> pageResponse = couponQueryService.getCoupons(search.name(), search.discountType(), search.visible(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "쿠폰 등록", description = "새로운 쿠폰을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        Long id = couponCommandService.createCoupon(
            request.name(),
            request.description(),
            request.discountType(),
            request.discountAmount(),
            request.maxDiscountAmount(),
            request.minOrderAmount(),
            request.maxDiscountCount(),
            request.issueStartAt(),
            request.issueEndAt(),
            request.useStartAt(),
            request.useEndAt(),
            request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "쿠폰 상세 조회", description = "쿠폰 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCoupon(@PathVariable Long id) {
        CouponDetailResponse response = couponQueryService.getCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "쿠폰 수정", description = "기존 쿠폰을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCoupon(
        @PathVariable Long id,
        @Valid @RequestBody CouponUpdateRequest request
    ) {
        couponCommandService.updateCoupon(
            id,
            request.name(),
            request.description(),
            request.discountType(),
            request.discountAmount(),
            request.maxDiscountAmount(),
            request.minOrderAmount(),
            request.maxDiscountCount(),
            request.issueStartAt(),
            request.issueEndAt(),
            request.useStartAt(),
            request.useEndAt(),
            request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "쿠폰 삭제", description = "기존 쿠폰을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponCommandService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "쿠폰 회원 발급", description = "특정 회원에게 쿠폰을 수동 발급합니다. 만료 일시는 쿠폰의 사용 종료 일시로 설정됩니다.")
    @PostMapping("/v1/{id}/issues")
    public ResponseEntity<ApiResponse<Long>> issueCoupon(
        @PathVariable Long id,
        @Valid @RequestBody CouponIssueRequest request
    ) {
        Long memberCouponId = couponCommandService.issueCoupon(id, request.memberId());
        return ResponseEntity.ok(ApiResponse.success(memberCouponId));
    }

    @Operation(summary = "쿠폰 발급 현황 조회", description = "특정 쿠폰의 회원 발급 현황을 페이징 조회합니다.")
    @GetMapping("/v1/{id}/issues")
    public ResponseEntity<ApiResponse<List<MemberCouponItemResponse>>> getIssuedCoupons(
        @PathVariable Long id,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<MemberCouponItemResponse> pageResponse = couponQueryService.getIssuedCoupons(id, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }
}
