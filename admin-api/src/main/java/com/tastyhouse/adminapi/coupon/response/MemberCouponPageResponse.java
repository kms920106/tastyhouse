package com.tastyhouse.adminapi.coupon.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "쿠폰 발급 현황 페이지 응답")
public record MemberCouponPageResponse(
    @Schema(description = "발급 현황 목록")
    List<MemberCouponAdminItemResponse> content,

    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {

    public static MemberCouponPageResponse from(PageResult<MemberCouponAdminItemResponse> pageResult) {
        return new MemberCouponPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
