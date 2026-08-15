package com.tastyhouse.adminapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.config.security.CustomUserDetails;
import com.tastyhouse.adminapi.shop.request.ShopNoticeHideRequest;
import com.tastyhouse.adminapi.shop.request.ShopNoticeSearchRequest;
import com.tastyhouse.adminapi.shop.response.ShopNoticeManagementListItemResponse;

/**
 * 점주 공지 검수 관리자 API.
 *
 * <p>경로에 가게 ID가 없다 — 관리자는 전체 공지를 가로질러 검수하므로 {@code noticeId}(전역 유니크 PK)
 * 단독으로 대상을 특정한다("컨트롤러 미사용 {@code @PathVariable} 경로 평탄화 규칙"). 소유권 검증 자체가
 * 관리자에게는 적용되지 않으므로, 평탄화가 검증 생략으로 이어지는 IDOR 위험도 없다.
 */
@Tag(name = "Shop Notice Admin", description = "점주 공지 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopNoticeAdminApiController {

    private final ShopNoticeQueryService shopNoticeQueryService;
    private final ShopNoticeCommandService shopNoticeCommandService;

    public ShopNoticeAdminApiController(ShopNoticeQueryService shopNoticeQueryService, ShopNoticeCommandService shopNoticeCommandService) {
        this.shopNoticeQueryService = shopNoticeQueryService;
        this.shopNoticeCommandService = shopNoticeCommandService;
    }

    @Operation(summary = "점주 공지 목록 조회", description = "전체 가게의 점주 공지를 조건 페이징 조회합니다. shopId/shopName/hidden은 필터(미지정 시 전체)입니다.")
    @GetMapping("/v1/notices")
    public ResponseEntity<ApiResponse<List<ShopNoticeManagementListItemResponse>>> getNotices(
        @Valid @ModelAttribute ShopNoticeSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopNoticeManagementListItemResponse> pageResponse = shopNoticeQueryService.getNotices(
            search.shopId(), search.shopName(), search.hidden(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "점주 공지 게시중단", description = "규정을 위반한 점주 공지를 게시중단합니다. 사유는 가게 변경이력에 남습니다.")
    @PutMapping("/v1/notices/{noticeId}/hide")
    public ResponseEntity<ApiResponse<Void>> hideNotice(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long noticeId,
        @Valid @RequestBody ShopNoticeHideRequest request
    ) {
        shopNoticeCommandService.hideNotice(userDetails.getPrincipalId(), noticeId, request.reason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "점주 공지 게시중단 해제", description = "게시중단된 점주 공지를 다시 게시합니다. 점주가 설정한 노출 여부가 그대로 복원됩니다.")
    @PutMapping("/v1/notices/{noticeId}/unhide")
    public ResponseEntity<ApiResponse<Void>> unhideNotice(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long noticeId
    ) {
        shopNoticeCommandService.unhideNotice(userDetails.getPrincipalId(), noticeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
