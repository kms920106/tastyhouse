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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeOwnerQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopNoticeCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopNoticeExposureRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopNoticeUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopNoticeValidateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopNoticeResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeOwnerCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeExposureChangeCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeUpdateCommand;

@Tag(name = "Ceo Shop Notice", description = "점주 가게 공지(사장님 공지) API")
@RestController
@RequestMapping("/api/shops")
public class ShopNoticeApiController {

    private final ShopNoticeOwnerQueryUseCase shopNoticeQueryService;
    private final ShopNoticeOwnerCommandUseCase shopNoticeCommandUseCase;

    public ShopNoticeApiController(ShopNoticeOwnerQueryUseCase shopNoticeQueryService, ShopNoticeOwnerCommandUseCase shopNoticeCommandUseCase) {
        this.shopNoticeQueryService = shopNoticeQueryService;
        this.shopNoticeCommandUseCase = shopNoticeCommandUseCase;
    }

    @Operation(summary = "공지 목록 조회", description = "가게의 점주 공지 목록을 조회합니다. 앱에 노출 중인 공지가 맨 위로 정렬됩니다.")
    @GetMapping("/v1/{id}/notices")
    public ResponseEntity<ApiResponse<List<ShopNoticeResponse>>> getNotices(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopNoticeResponse> response = shopNoticeQueryService.getNotices(userDetails.getCeoId(), id).stream()
            .map(ShopNoticeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지 등록", description = "가게에 점주 공지를 등록합니다. 이미지는 최대 3장이며, exposed=true면 등록과 동시에 앱에 반영됩니다.")
    @PostMapping(value = "/v1/{id}/notices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopNoticeCreateRequest request
    ) {
        ShopNoticeCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long noticeId = shopNoticeCommandUseCase.createNotice(command, request.files());
        return ResponseEntity.ok(ApiResponse.success(noticeId));
    }

    @Operation(summary = "공지 수정", description = "등록된 점주 공지를 수정합니다. 이미지는 replace-all로 교체하며, keepExistingImages=true면 본문만 수정합니다.")
    @PutMapping(value = "/v1/{id}/notices/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long noticeId,
        @Valid @ModelAttribute ShopNoticeUpdateRequest request
    ) {
        ShopNoticeUpdateCommand command = request.toCommand(userDetails.getCeoId(), id, noticeId);
        shopNoticeCommandUseCase.updateNotice(command, request.files());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지 삭제", description = "등록된 점주 공지를 첨부 이미지와 함께 삭제합니다.")
    @DeleteMapping("/v1/{id}/notices/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long noticeId
    ) {
        ShopNoticeDeleteCommand command = ShopNoticeDeleteCommand.of(userDetails.getCeoId(), id, noticeId);
        shopNoticeCommandUseCase.deleteNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지 앱 노출 토글", description = "공지를 앱에 반영하거나 내립니다. 반영 시 기존에 노출 중이던 공지는 자동으로 내려갑니다(가게당 1건).")
    @PutMapping("/v1/{id}/notices/{noticeId}/exposure")
    public ResponseEntity<ApiResponse<Void>> changeExposure(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long noticeId,
        @Valid @RequestBody ShopNoticeExposureRequest request
    ) {
        ShopNoticeExposureChangeCommand command = request.toCommand(userDetails.getCeoId(), id, noticeId);
        shopNoticeCommandUseCase.changeExposure(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지 금칙어 검수", description = "등록 전 공지 본문에 금칙어가 포함되어 있는지 미리 검수해 위반 단어 목록을 반환합니다.")
    @PostMapping("/v1/{id}/notices/validate")
    public ResponseEntity<ApiResponse<List<String>>> validateNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopNoticeValidateRequest request
    ) {
        List<String> violations = shopNoticeQueryService.validateNotice(userDetails.getCeoId(), id, request.content());
        return ResponseEntity.ok(ApiResponse.success(violations));
    }
}
