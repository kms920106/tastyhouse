package com.tastyhouse.adminapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.request.ShopContentBoardHideRequest;
import com.tastyhouse.adminapi.shop.request.ShopContentBoardSearchRequest;
import com.tastyhouse.adminapi.shop.response.ShopContentBoardListItemResponse;

@Tag(name = "Shop Content Board Admin", description = "가게 콘텐츠보드 검수 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopContentBoardAdminApiController {

    private final ShopContentBoardQueryService shopContentBoardQueryService;
    private final ShopContentBoardCommandService shopContentBoardCommandService;

    @Operation(summary = "콘텐츠보드 목록 조회", description = "전체 가게 콘텐츠보드를 조건 페이징 조회합니다. shopId/hidden/contentType은 필터(미지정 시 전체)입니다.")
    @GetMapping("/v1/content-boards")
    public ResponseEntity<ApiResponse<List<ShopContentBoardListItemResponse>>> getContentBoards(
        @Valid @ModelAttribute ShopContentBoardSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopContentBoardListItemResponse> pageResponse = shopContentBoardQueryService.getContentBoards(
            search.shopId(), search.hidden(), search.contentType(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "콘텐츠보드 숨김 처리", description = "가게 콘텐츠보드를 숨김/숨김해제 처리합니다.")
    @PatchMapping("/v1/content-boards/{contentBoardId}/hide")
    public ResponseEntity<ApiResponse<Void>> changeHidden(
        @PathVariable Long contentBoardId,
        @Valid @RequestBody ShopContentBoardHideRequest request
    ) {
        shopContentBoardCommandService.changeHidden(contentBoardId, request.hidden());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "콘텐츠보드 삭제", description = "가게 콘텐츠보드를 삭제합니다.")
    @DeleteMapping("/v1/content-boards/{contentBoardId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentBoard(@PathVariable Long contentBoardId) {
        shopContentBoardCommandService.deleteContentBoard(contentBoardId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
