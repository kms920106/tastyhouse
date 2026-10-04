package com.tastyhouse.adminapi.shop.adapter.in.web;

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

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopChoiceCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopChoiceSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceListItemResponse;

@Tag(name = "Shop Choice Admin", description = "가게 테하 초이스 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopChoiceAdminApiController {

    private final ShopChoiceCreateUseCase shopChoiceCreateUseCase;
    private final ShopChoiceUpdateUseCase shopChoiceUpdateUseCase;
    private final ShopChoiceDeleteUseCase shopChoiceDeleteUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopChoiceAdminApiController(
        ShopChoiceCreateUseCase shopChoiceCreateUseCase,
        ShopChoiceUpdateUseCase shopChoiceUpdateUseCase,
        ShopChoiceDeleteUseCase shopChoiceDeleteUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopChoiceCreateUseCase = shopChoiceCreateUseCase;
        this.shopChoiceUpdateUseCase = shopChoiceUpdateUseCase;
        this.shopChoiceDeleteUseCase = shopChoiceDeleteUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "테하 초이스 목록 조회", description = "테하 초이스 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/editor-choices")
    public ResponseEntity<ApiResponse<List<ShopChoiceListItemResponse>>> getShopChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<EditorChoiceResult> pageResult = shopQueryUseCase.getShopChoices(pageRequest.page(), pageRequest.size());
        PaginationResponse<ShopChoiceListItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(ShopChoiceListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "테하 초이스 등록", description = "새로운 테하 초이스를 등록합니다.")
    @PostMapping("/v1/editor-choices")
    public ResponseEntity<ApiResponse<Long>> createShopChoice(@Valid @RequestBody ShopChoiceCreateRequest request) {
        ShopChoiceCreateCommand command = request.toCommand();
        Long id = shopChoiceCreateUseCase.createShopChoice(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "테하 초이스 상세 조회", description = "테하 초이스 상세를 조회합니다.")
    @GetMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<ShopChoiceDetailResponse>> getShopChoice(@PathVariable Long choiceId) {
        ShopChoiceDetailResponse response = ShopChoiceDetailResponse.from(shopQueryUseCase.getShopChoice(choiceId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테하 초이스 수정", description = "등록된 테하 초이스를 수정합니다.")
    @PutMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<Void>> updateShopChoice(
        @PathVariable Long choiceId,
        @Valid @RequestBody ShopChoiceSaveRequest request
    ) {
        ShopChoiceUpdateCommand command = request.toCommand(choiceId);
        shopChoiceUpdateUseCase.updateShopChoice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "테하 초이스 삭제", description = "등록된 테하 초이스를 삭제합니다.")
    @DeleteMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<Void>> deleteShopChoice(@PathVariable Long choiceId) {
        ShopChoiceDeleteCommand command = ShopChoiceDeleteCommand.of(choiceId);
        shopChoiceDeleteUseCase.deleteShopChoice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
