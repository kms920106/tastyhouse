package com.tastyhouse.adminapi.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.shop.request.ShopContentBoardHideRequest;

@Tag(name = "Shop Content Board Admin", description = "가게 콘텐츠보드 검수 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopContentBoardAdminApiController {

    private final ShopContentBoardAdminService shopContentBoardAdminService;

    @Operation(summary = "콘텐츠보드 숨김 처리", description = "가게 콘텐츠보드를 숨김/숨김해제 처리합니다.")
    @PatchMapping("/v1/content-boards/{contentBoardId}/hide")
    public ResponseEntity<ApiResponse<Void>> changeHidden(
        @PathVariable Long contentBoardId,
        @Valid @RequestBody ShopContentBoardHideRequest request
    ) {
        shopContentBoardAdminService.changeHidden(contentBoardId, request.hidden());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "콘텐츠보드 삭제", description = "가게 콘텐츠보드를 삭제합니다.")
    @DeleteMapping("/v1/content-boards/{contentBoardId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentBoard(@PathVariable Long contentBoardId) {
        shopContentBoardAdminService.deleteContentBoard(contentBoardId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
