package com.tastyhouse.webapi.member.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.coupon.port.in.CouponMyAvailableListQueryUseCase;
import com.tastyhouse.application.coupon.port.in.CouponMyListQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberMyBookmarkedShopListQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberMyReviewCountQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberMyReviewListQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyCouponListItemResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.ShopBookmarkListItemResponse;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Me Activity", description = "내 활동 조회 API")
class MemberMeActivityApiController {

    private final CouponMyListQueryUseCase couponMyListQueryUseCase;
    private final CouponMyAvailableListQueryUseCase couponMyAvailableListQueryUseCase;
    private final MemberMyReviewCountQueryUseCase memberMyReviewCountQueryUseCase;
    private final MemberMyReviewListQueryUseCase memberMyReviewListQueryUseCase;
    private final MemberMyBookmarkedShopListQueryUseCase memberMyBookmarkedShopListQueryUseCase;

    public MemberMeActivityApiController(
        CouponMyListQueryUseCase couponMyListQueryUseCase,
        CouponMyAvailableListQueryUseCase couponMyAvailableListQueryUseCase,
        MemberMyReviewCountQueryUseCase memberMyReviewCountQueryUseCase,
        MemberMyReviewListQueryUseCase memberMyReviewListQueryUseCase,
        MemberMyBookmarkedShopListQueryUseCase memberMyBookmarkedShopListQueryUseCase
    ) {
        this.couponMyListQueryUseCase = couponMyListQueryUseCase;
        this.couponMyAvailableListQueryUseCase = couponMyAvailableListQueryUseCase;
        this.memberMyReviewCountQueryUseCase = memberMyReviewCountQueryUseCase;
        this.memberMyReviewListQueryUseCase = memberMyReviewListQueryUseCase;
        this.memberMyBookmarkedShopListQueryUseCase = memberMyBookmarkedShopListQueryUseCase;
    }

    @Operation(summary = "보유 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 모든 쿠폰을 조회합니다. (사용 여부 무관)")
    @GetMapping("/v1/me/coupons")
    public ResponseEntity<ApiResponse<List<MyCouponListItemResponse>>> getMyCoupons(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            couponMyListQueryUseCase.getMyCoupons(userDetails.getMemberId())
                .stream()
                .map(MyCouponListItemResponse::from)
                .toList()
        ));
    }

    @Operation(summary = "사용 가능한 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 사용 가능한 쿠폰을 조회합니다. (미사용 + 유효기간 내)")
    @GetMapping("/v1/me/coupons/available")
    public ResponseEntity<ApiResponse<List<MyCouponListItemResponse>>> getMyAvailableCoupons(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            couponMyAvailableListQueryUseCase.getMyAvailableCoupons(userDetails.getMemberId())
                .stream()
                .map(MyCouponListItemResponse::from)
                .toList()
        ));
    }

    @Operation(summary = "내가 작성한 리뷰 개수 조회", description = "로그인한 회원이 작성한 리뷰 개수를 조회합니다.")
    @GetMapping("/v1/me/reviews/count")
    public ResponseEntity<ApiResponse<MyReviewCountResponse>> getMyReviewCount(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(MyReviewCountResponse.from(memberMyReviewCountQueryUseCase.getMyReviewCount(userDetails.getMemberId()))));
    }

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/me/reviews")
    public ResponseEntity<ApiResponse<List<MyReviewListItemResponse>>> getMyReviews(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<MyReviewListItemResponse> pageResult = PaginationResponse.from(
            memberMyReviewListQueryUseCase.getMyReviews(userDetails.getMemberId(), pageRequest.page(), pageRequest.size())
                .map(MyReviewListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "내가 즐겨찾기한 가게 목록 조회", description = "로그인한 회원이 북마크한 가게 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/me/bookmarks")
    public ResponseEntity<ApiResponse<List<ShopBookmarkListItemResponse>>> getMyBookmarkedShops(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopBookmarkListItemResponse> pageResult = PaginationResponse.from(
            memberMyBookmarkedShopListQueryUseCase.getMyBookmarkedShops(userDetails.getMemberId(), pageRequest.page(), pageRequest.size())
                .map(ShopBookmarkListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }
}
