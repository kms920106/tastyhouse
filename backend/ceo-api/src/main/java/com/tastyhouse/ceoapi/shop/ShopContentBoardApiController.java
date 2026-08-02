package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopContentBoardCreateRequest;
import com.tastyhouse.ceoapi.shop.request.ShopContentBoardUpdateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopContentBoardResponse;

@Tag(name = "Ceo Shop Content Board", description = "점주 가게 콘텐츠보드 API")
@RestController
@RequestMapping("/api/shops")
public class ShopContentBoardApiController {

    private final ShopContentBoardQueryService shopContentBoardQueryService;
    private final ShopContentBoardCommandService shopContentBoardCommandService;

    public ShopContentBoardApiController(ShopContentBoardQueryService shopContentBoardQueryService, ShopContentBoardCommandService shopContentBoardCommandService) {
        this.shopContentBoardQueryService = shopContentBoardQueryService;
        this.shopContentBoardCommandService = shopContentBoardCommandService;
    }

    @Operation(summary = "콘텐츠보드 목록 조회", description = "가게의 콘텐츠보드 목록을 조회합니다.")
    @GetMapping("/v1/{id}/content-boards")
    public ResponseEntity<ApiResponse<List<ShopContentBoardResponse>>> getContentBoards(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopContentBoardResponse> response = shopContentBoardQueryService.getContentBoards(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "콘텐츠보드 등록", description = "가게에 콘텐츠보드를 등록합니다. (최대 4개, IMAGE/GIF는 file, VIDEO는 youtubeUrl 사용)")
    @PostMapping(value = "/v1/{id}/content-boards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createContentBoard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopContentBoardCreateRequest request
    ) {
        Long contentBoardId = shopContentBoardCommandService.createContentBoard(
            userDetails.getCeoId(),
            id,
            request.contentType(),
            request.topic(),
            request.file(),
            request.youtubeUrl(),
            request.description()
        );
        return ResponseEntity.ok(ApiResponse.success(contentBoardId));
    }

    @Operation(summary = "콘텐츠보드 수정", description = "등록된 콘텐츠보드를 수정합니다.")
    @PutMapping(value = "/v1/{id}/content-boards/{contentBoardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateContentBoard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long contentBoardId,
        @Valid @ModelAttribute ShopContentBoardUpdateRequest request
    ) {
        shopContentBoardCommandService.updateContentBoard(
            userDetails.getCeoId(),
            id,
            contentBoardId,
            request.topic(),
            request.file(),
            request.youtubeUrl(),
            request.description()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "콘텐츠보드 삭제", description = "등록된 콘텐츠보드를 삭제합니다.")
    @DeleteMapping("/v1/{id}/content-boards/{contentBoardId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentBoard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long contentBoardId
    ) {
        shopContentBoardCommandService.deleteContentBoard(userDetails.getCeoId(), id, contentBoardId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
