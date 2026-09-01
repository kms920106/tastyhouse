package com.tastyhouse.ceoapi.shop.adapter.in.web;

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

import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopContentBoardCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopContentBoardUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopContentBoardResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardUpdateCommand;

@Tag(name = "Ceo Shop Content Board", description = "점주 가게 콘텐츠보드 API")
@RestController
@RequestMapping("/api/shops")
public class ShopContentBoardApiController {

    private final ShopContentBoardQueryUseCase shopContentBoardQueryService;
    private final ShopContentBoardCommandUseCase shopContentBoardCommandUseCase;

    public ShopContentBoardApiController(ShopContentBoardQueryUseCase shopContentBoardQueryService, ShopContentBoardCommandUseCase shopContentBoardCommandUseCase) {
        this.shopContentBoardQueryService = shopContentBoardQueryService;
        this.shopContentBoardCommandUseCase = shopContentBoardCommandUseCase;
    }

    @Operation(summary = "콘텐츠보드 목록 조회", description = "가게의 콘텐츠보드 목록을 조회합니다.")
    @GetMapping("/v1/{id}/content-boards")
    public ResponseEntity<ApiResponse<List<ShopContentBoardResponse>>> getContentBoards(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopContentBoardResponse> response = shopContentBoardQueryService.getContentBoards(userDetails.getCeoId(), id).stream()
            .map(ShopContentBoardResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "콘텐츠보드 등록", description = "가게에 콘텐츠보드를 등록합니다. (최대 4개, IMAGE/GIF는 file, VIDEO는 youtubeUrl 사용)")
    @PostMapping(value = "/v1/{id}/content-boards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createContentBoard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopContentBoardCreateRequest request
    ) {
        ShopContentBoardCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long contentBoardId = shopContentBoardCommandUseCase.createContentBoard(command, request.file());
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
        ShopContentBoardUpdateCommand command = request.toCommand(userDetails.getCeoId(), id, contentBoardId);
        shopContentBoardCommandUseCase.updateContentBoard(command, request.file());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "콘텐츠보드 삭제", description = "등록된 콘텐츠보드를 삭제합니다.")
    @DeleteMapping("/v1/{id}/content-boards/{contentBoardId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentBoard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long contentBoardId
    ) {
        ShopContentBoardDeleteCommand command = ShopContentBoardDeleteCommand.of(userDetails.getCeoId(), id, contentBoardId);
        shopContentBoardCommandUseCase.deleteContentBoard(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
