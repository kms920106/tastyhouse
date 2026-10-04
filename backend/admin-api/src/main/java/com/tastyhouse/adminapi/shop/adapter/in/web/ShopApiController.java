package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeUseCase;
import com.tastyhouse.application.shop.port.in.ShopCloseCommand;
import com.tastyhouse.application.shop.port.in.ShopCloseUseCase;
import com.tastyhouse.application.shop.port.in.ShopCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeUseCase;
import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopUpdateUseCase;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCeoAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCupDepositRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.StationResponse;

@Tag(name = "Shop Admin", description = "가게 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopApiController {

    private final ShopCreateUseCase shopCreateUseCase;
    private final ShopCeoAssignUseCase shopCeoAssignUseCase;
    private final ShopCeoRevokeUseCase shopCeoRevokeUseCase;
    private final ShopUpdateUseCase shopUpdateUseCase;
    private final ShopCloseUseCase shopCloseUseCase;
    private final ShopCupDepositChangeUseCase shopCupDepositChangeUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopApiController(
        ShopCreateUseCase shopCreateUseCase,
        ShopCeoAssignUseCase shopCeoAssignUseCase,
        ShopCeoRevokeUseCase shopCeoRevokeUseCase,
        ShopUpdateUseCase shopUpdateUseCase,
        ShopCloseUseCase shopCloseUseCase,
        ShopCupDepositChangeUseCase shopCupDepositChangeUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopCreateUseCase = shopCreateUseCase;
        this.shopCeoAssignUseCase = shopCeoAssignUseCase;
        this.shopCeoRevokeUseCase = shopCeoRevokeUseCase;
        this.shopUpdateUseCase = shopUpdateUseCase;
        this.shopCloseUseCase = shopCloseUseCase;
        this.shopCupDepositChangeUseCase = shopCupDepositChangeUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "지하철역 목록 조회", description = "가게 등록·수정 시 선택 가능한 지하철역 목록을 조회합니다.")
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<StationResponse>>> getStations() {
        List<StationResponse> response = shopQueryUseCase.getStations().stream()
            .map(StationResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 목록 조회", description = "가게 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ShopListItemResponse>>> getShops(
        @Valid @ModelAttribute ShopSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ShopListItemResult> pageResult = shopQueryUseCase.getShops(
            search.name(), search.stationId(), search.permanentlyClosed(),
            pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<ShopListItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(ShopListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "가게 등록", description = "새로운 가게를 등록합니다. 담당 점주를 함께 지정하면 시스템 접근권한 부여 이력이 기록됩니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createShop(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @Valid @RequestBody ShopCreateRequest request
    ) {
        ShopCreateCommand command = request.toCommand(userDetails.getPrincipalId());
        Long id = shopCreateUseCase.createShop(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(
        summary = "가게 담당 점주 배정",
        description = "가게에 담당 점주를 배정하고 시스템 접근권한 부여 이력을 기록합니다. 다른 점주가 이미 배정돼 있으면 말소 후 부여로 2건이 기록됩니다."
    )
    @PutMapping("/v1/{id}/ceo")
    public ResponseEntity<ApiResponse<Void>> assignCeo(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopCeoAssignRequest request
    ) {
        ShopCeoAssignCommand command = request.toCommand(userDetails.getPrincipalId(), id);
        shopCeoAssignUseCase.assignCeo(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "가게 담당 점주 해제",
        description = "가게의 담당 점주 배정을 해제하고 시스템 접근권한 말소 이력을 기록합니다."
    )
    @DeleteMapping("/v1/{id}/ceo")
    public ResponseEntity<ApiResponse<Void>> revokeCeo(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopCeoRevokeCommand command = ShopCeoRevokeCommand.of(userDetails.getPrincipalId(), id);
        shopCeoRevokeUseCase.revokeCeo(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 상세 조회", description = "가게 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShop(@PathVariable Long id) {
        ShopManagementQueryUseCase.ShopDetail detail = shopQueryUseCase.getShop(id);
        ShopDetailResponse response = ShopDetailResponse.from(detail.shop(), detail.thumbnailImageUrl());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 수정", description = "기존 가게를 수정합니다. 폐업 처리된 가게는 수정할 수 없습니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateShop(
        @PathVariable Long id,
        @Valid @RequestBody ShopUpdateRequest request
    ) {
        ShopUpdateCommand command = request.toCommand(id);
        shopUpdateUseCase.updateShop(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 폐업 처리", description = "가게를 폐업 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeShop(@PathVariable Long id) {
        ShopCloseCommand command = ShopCloseCommand.of(id);
        shopCloseUseCase.closeShop(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "일회용컵 보증금제 대상 사업자 지정/해제",
        description = "환경부·자원순환보증금관리센터가 지정하는 외부 규제 사실을 반영합니다. 점주는 "
            + "변경할 수 없습니다. 켜야 그 가게가 보증금 옵션그룹을 만들 수 있으며, 끄더라도 이미 만들어진 "
            + "보증금 옵션그룹의 조회·주문은 계속 동작합니다(지정 해제 시 유예).")
    @PatchMapping("/v1/{id}/cup-deposit")
    public ResponseEntity<ApiResponse<Void>> changeCupDepositEnabled(
        @PathVariable Long id,
        @Valid @RequestBody ShopCupDepositRequest request
    ) {
        ShopCupDepositChangeCommand command = request.toCommand(id);
        shopCupDepositChangeUseCase.changeCupDepositEnabled(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
