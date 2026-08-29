package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderGuideCommandUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderPickupLocationUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderVisitGuideDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderVisitGuideRevisionCommand;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.config.security.CustomUserDetails;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopRiderGuideSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopRiderPickupLocationUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopRiderVisitGuideDeleteRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopRiderVisitGuideRevisionRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopRiderGuideDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopRiderGuideListItemResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderGuideQueryUseCase;

@Tag(name = "Shop Rider Guide Admin", description = "라이더 가게방문 안내 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopRiderGuideAdminApiController {

    private final ShopRiderGuideQueryUseCase shopRiderGuideQueryUseCase;
    private final ShopRiderGuideCommandUseCase shopRiderGuideCommandUseCase;

    public ShopRiderGuideAdminApiController(ShopRiderGuideQueryUseCase shopRiderGuideQueryUseCase, ShopRiderGuideCommandUseCase shopRiderGuideCommandUseCase) {
        this.shopRiderGuideQueryUseCase = shopRiderGuideQueryUseCase;
        this.shopRiderGuideCommandUseCase = shopRiderGuideCommandUseCase;
    }

    @Operation(summary = "라이더 안내 등록 가게 목록 조회",
        description = "라이더 안내가 등록된 가게를 최근 변경순으로 페이징 조회합니다. shopName/hasVisitGuide는 필터(미지정 시 전체)입니다.")
    @GetMapping("/v1/rider-guides")
    public ResponseEntity<ApiResponse<List<ShopRiderGuideListItemResponse>>> getRiderGuides(
        @Valid @ModelAttribute ShopRiderGuideSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopRiderGuideListItemResponse> pageResponse = shopRiderGuideQueryUseCase.getRiderGuides(
            search.shopName(), search.hasVisitGuide(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "라이더 안내 상세 조회",
        description = "가게 단건의 라이더 안내 문구·픽업 위치와 최근 변경 이력(최대 20건)을 조회합니다.")
    @GetMapping("/v1/{id}/rider-guide")
    public ResponseEntity<ApiResponse<ShopRiderGuideDetailResponse>> getRiderGuide(@PathVariable Long id) {
        ShopRiderGuideDetailResponse response = shopRiderGuideQueryUseCase.getRiderGuide(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "부적합 라이더 안내 문구 삭제 조치",
        description = "등록 기준에 벗어난 안내 문구를 삭제하고 사유와 함께 이력을 남깁니다. 픽업 위치는 유지됩니다.")
    @DeleteMapping("/v1/{id}/rider-guide/visit-guide")
    public ResponseEntity<ApiResponse<Void>> deleteVisitGuide(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderVisitGuideDeleteRequest request
    ) {
        ShopRiderVisitGuideDeleteCommand command = request.toCommand(id, userDetails.getPrincipalId());
        shopRiderGuideCommandUseCase.deleteVisitGuide(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "라이더 안내 문구 수정 요청",
        description = "안내 문구는 그대로 두고 수정 요청 이력만 남깁니다. 점주 알림 발송은 후속 과제입니다.")
    @PostMapping("/v1/{id}/rider-guide/visit-guide/revision-request")
    public ResponseEntity<ApiResponse<Long>> requestRevision(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderVisitGuideRevisionRequest request
    ) {
        ShopRiderVisitGuideRevisionCommand command = request.toCommand(id, userDetails.getPrincipalId());
        Long historyId = shopRiderGuideCommandUseCase.requestRevision(command);
        return ResponseEntity.ok(ApiResponse.success(historyId));
    }

    @Operation(summary = "라이더 픽업 위치 교정",
        description = "라이더 제보를 반영해 픽업 위치를 관리자가 직접 수정합니다. 가게 실주소·좌표는 변경되지 않습니다.")
    @PutMapping("/v1/{id}/rider-guide/pickup-location")
    public ResponseEntity<ApiResponse<Void>> updatePickupLocation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderPickupLocationUpdateRequest request
    ) {
        ShopRiderPickupLocationUpdateCommand command = request.toCommand(id, userDetails.getPrincipalId());
        shopRiderGuideCommandUseCase.updatePickupLocation(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
