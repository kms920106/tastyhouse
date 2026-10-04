package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.shop.port.in.ShopBookmarkToggleCommand;
import com.tastyhouse.application.shop.port.in.ShopCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDetailQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopBookmarkResponse;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop Bookmark", description = "가게 북마크 API")
class ShopBookmarkApiController {

    private final ShopCommandUseCase shopCommandUseCase;
    private final ShopDetailQueryUseCase shopDetailQueryUseCase;

    public ShopBookmarkApiController(
        ShopCommandUseCase shopCommandUseCase,
        ShopDetailQueryUseCase shopDetailQueryUseCase
    ) {
        this.shopCommandUseCase = shopCommandUseCase;
        this.shopDetailQueryUseCase = shopDetailQueryUseCase;
    }

    @Operation(summary = "북마크 여부 조회", description = "가게가 현재 사용자에 의해 북마크되었는지 여부를 조회합니다.")
    @GetMapping("/v1/{id}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> isBookmarked(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ShopBookmarkResponse bookmarked;
        if (userDetails == null) {
            bookmarked = ShopBookmarkResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            bookmarked = ShopBookmarkResponse.from(shopDetailQueryUseCase.isBookmarked(id, memberId));
        }
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "북마크 토글", description = "가게에 대한 북마크를 추가하거나 제거합니다.")
    @PostMapping("/v1/{id}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> toggleBookmark(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        ShopBookmarkToggleCommand command = ShopBookmarkToggleCommand.of(userDetails.getMemberId(), id);
        boolean bookmarked = shopCommandUseCase.toggleBookmark(command);
        return ResponseEntity.ok(ApiResponse.success(ShopBookmarkResponse.from(bookmarked)));
    }
}
