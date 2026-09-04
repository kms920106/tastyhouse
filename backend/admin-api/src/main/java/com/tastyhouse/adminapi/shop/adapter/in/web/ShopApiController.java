package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityManagementAssignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityAssignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityCategoryCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityCategoryCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityCategoryUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityCategoryUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityManagementUnassignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopAmenityUnassignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBannerImageCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBannerImageCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeManagementCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeManagementDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeManagementUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBreakTimeUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourManagementCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourManagementDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourManagementUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopBusinessHourUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopCeoAssignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopCeoAssignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopCeoRevokeCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopCeoRevokeUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopCloseCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopCloseUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopClosedDayManagementCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopClosedDayCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopClosedDayManagementDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopClosedDayDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopCupDepositChangeCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopCupDepositChangeUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeAssignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeAssignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeCategoryCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeCategoryCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeCategoryUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeCategoryUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderMethodAssignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderMethodAssignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopUpdateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopUpdateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.TagCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.TagCreateUseCase;
import com.tastyhouse.adminapplication.shop.port.in.TagDeleteCommand;
import com.tastyhouse.adminapplication.shop.port.in.TagDeleteUseCase;

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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.auth.security.AdminUserDetails;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBannerImageSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBreakTimeSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBusinessHourSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCeoAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopChoiceCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopChoiceSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopClosedDaySaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopCupDepositRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopOrderMethodAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopPhotoCategoryImageSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopPhotoCategorySaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.TagCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBannerImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBreakTimeResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBusinessHourResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopClosedDayResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderMethodItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.StationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.TagResponse;
import com.tastyhouse.adminapplication.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

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
    private final ShopBusinessHourCreateUseCase shopBusinessHourCreateUseCase;
    private final ShopBusinessHourUpdateUseCase shopBusinessHourUpdateUseCase;
    private final ShopBusinessHourDeleteUseCase shopBusinessHourDeleteUseCase;
    private final ShopBreakTimeCreateUseCase shopBreakTimeCreateUseCase;
    private final ShopBreakTimeUpdateUseCase shopBreakTimeUpdateUseCase;
    private final ShopBreakTimeDeleteUseCase shopBreakTimeDeleteUseCase;
    private final ShopClosedDayCreateUseCase shopClosedDayCreateUseCase;
    private final ShopClosedDayDeleteUseCase shopClosedDayDeleteUseCase;
    private final ShopAmenityCategoryCreateUseCase shopAmenityCategoryCreateUseCase;
    private final ShopAmenityCategoryUpdateUseCase shopAmenityCategoryUpdateUseCase;
    private final ShopFoodTypeCategoryCreateUseCase shopFoodTypeCategoryCreateUseCase;
    private final ShopFoodTypeCategoryUpdateUseCase shopFoodTypeCategoryUpdateUseCase;
    private final ShopAmenityAssignUseCase shopAmenityAssignUseCase;
    private final ShopAmenityUnassignUseCase shopAmenityUnassignUseCase;
    private final ShopFoodTypeAssignUseCase shopFoodTypeAssignUseCase;
    private final ShopFoodTypeUnassignUseCase shopFoodTypeUnassignUseCase;
    private final TagCreateUseCase tagCreateUseCase;
    private final TagDeleteUseCase tagDeleteUseCase;
    private final ShopOrderMethodAssignUseCase shopOrderMethodAssignUseCase;
    private final ShopOrderMethodUnassignUseCase shopOrderMethodUnassignUseCase;
    private final ShopBannerImageCreateUseCase shopBannerImageCreateUseCase;
    private final ShopBannerImageDeleteUseCase shopBannerImageDeleteUseCase;
    private final ShopPhotoCategoryCreateUseCase shopPhotoCategoryCreateUseCase;
    private final ShopPhotoCategoryUpdateUseCase shopPhotoCategoryUpdateUseCase;
    private final ShopPhotoCategoryDeleteUseCase shopPhotoCategoryDeleteUseCase;
    private final ShopPhotoCategoryImageCreateUseCase shopPhotoCategoryImageCreateUseCase;
    private final ShopPhotoCategoryImageUpdateUseCase shopPhotoCategoryImageUpdateUseCase;
    private final ShopPhotoCategoryImageDeleteUseCase shopPhotoCategoryImageDeleteUseCase;
    private final ShopChoiceCreateUseCase shopChoiceCreateUseCase;
    private final ShopChoiceUpdateUseCase shopChoiceUpdateUseCase;
    private final ShopChoiceDeleteUseCase shopChoiceDeleteUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopApiController(
        ShopCreateUseCase shopCreateUseCase,
        ShopCeoAssignUseCase shopCeoAssignUseCase,
        ShopCeoRevokeUseCase shopCeoRevokeUseCase,
        ShopUpdateUseCase shopUpdateUseCase,
        ShopCloseUseCase shopCloseUseCase,
        ShopCupDepositChangeUseCase shopCupDepositChangeUseCase,
        ShopBusinessHourCreateUseCase shopBusinessHourCreateUseCase,
        ShopBusinessHourUpdateUseCase shopBusinessHourUpdateUseCase,
        ShopBusinessHourDeleteUseCase shopBusinessHourDeleteUseCase,
        ShopBreakTimeCreateUseCase shopBreakTimeCreateUseCase,
        ShopBreakTimeUpdateUseCase shopBreakTimeUpdateUseCase,
        ShopBreakTimeDeleteUseCase shopBreakTimeDeleteUseCase,
        ShopClosedDayCreateUseCase shopClosedDayCreateUseCase,
        ShopClosedDayDeleteUseCase shopClosedDayDeleteUseCase,
        ShopAmenityCategoryCreateUseCase shopAmenityCategoryCreateUseCase,
        ShopAmenityCategoryUpdateUseCase shopAmenityCategoryUpdateUseCase,
        ShopFoodTypeCategoryCreateUseCase shopFoodTypeCategoryCreateUseCase,
        ShopFoodTypeCategoryUpdateUseCase shopFoodTypeCategoryUpdateUseCase,
        ShopAmenityAssignUseCase shopAmenityAssignUseCase,
        ShopAmenityUnassignUseCase shopAmenityUnassignUseCase,
        ShopFoodTypeAssignUseCase shopFoodTypeAssignUseCase,
        ShopFoodTypeUnassignUseCase shopFoodTypeUnassignUseCase,
        TagCreateUseCase tagCreateUseCase,
        TagDeleteUseCase tagDeleteUseCase,
        ShopOrderMethodAssignUseCase shopOrderMethodAssignUseCase,
        ShopOrderMethodUnassignUseCase shopOrderMethodUnassignUseCase,
        ShopBannerImageCreateUseCase shopBannerImageCreateUseCase,
        ShopBannerImageDeleteUseCase shopBannerImageDeleteUseCase,
        ShopPhotoCategoryCreateUseCase shopPhotoCategoryCreateUseCase,
        ShopPhotoCategoryUpdateUseCase shopPhotoCategoryUpdateUseCase,
        ShopPhotoCategoryDeleteUseCase shopPhotoCategoryDeleteUseCase,
        ShopPhotoCategoryImageCreateUseCase shopPhotoCategoryImageCreateUseCase,
        ShopPhotoCategoryImageUpdateUseCase shopPhotoCategoryImageUpdateUseCase,
        ShopPhotoCategoryImageDeleteUseCase shopPhotoCategoryImageDeleteUseCase,
        ShopChoiceCreateUseCase shopChoiceCreateUseCase,
        ShopChoiceUpdateUseCase shopChoiceUpdateUseCase,
        ShopChoiceDeleteUseCase shopChoiceDeleteUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopCreateUseCase = shopCreateUseCase;
        this.shopCeoAssignUseCase = shopCeoAssignUseCase;
        this.shopCeoRevokeUseCase = shopCeoRevokeUseCase;
        this.shopUpdateUseCase = shopUpdateUseCase;
        this.shopCloseUseCase = shopCloseUseCase;
        this.shopCupDepositChangeUseCase = shopCupDepositChangeUseCase;
        this.shopBusinessHourCreateUseCase = shopBusinessHourCreateUseCase;
        this.shopBusinessHourUpdateUseCase = shopBusinessHourUpdateUseCase;
        this.shopBusinessHourDeleteUseCase = shopBusinessHourDeleteUseCase;
        this.shopBreakTimeCreateUseCase = shopBreakTimeCreateUseCase;
        this.shopBreakTimeUpdateUseCase = shopBreakTimeUpdateUseCase;
        this.shopBreakTimeDeleteUseCase = shopBreakTimeDeleteUseCase;
        this.shopClosedDayCreateUseCase = shopClosedDayCreateUseCase;
        this.shopClosedDayDeleteUseCase = shopClosedDayDeleteUseCase;
        this.shopAmenityCategoryCreateUseCase = shopAmenityCategoryCreateUseCase;
        this.shopAmenityCategoryUpdateUseCase = shopAmenityCategoryUpdateUseCase;
        this.shopFoodTypeCategoryCreateUseCase = shopFoodTypeCategoryCreateUseCase;
        this.shopFoodTypeCategoryUpdateUseCase = shopFoodTypeCategoryUpdateUseCase;
        this.shopAmenityAssignUseCase = shopAmenityAssignUseCase;
        this.shopAmenityUnassignUseCase = shopAmenityUnassignUseCase;
        this.shopFoodTypeAssignUseCase = shopFoodTypeAssignUseCase;
        this.shopFoodTypeUnassignUseCase = shopFoodTypeUnassignUseCase;
        this.tagCreateUseCase = tagCreateUseCase;
        this.tagDeleteUseCase = tagDeleteUseCase;
        this.shopOrderMethodAssignUseCase = shopOrderMethodAssignUseCase;
        this.shopOrderMethodUnassignUseCase = shopOrderMethodUnassignUseCase;
        this.shopBannerImageCreateUseCase = shopBannerImageCreateUseCase;
        this.shopBannerImageDeleteUseCase = shopBannerImageDeleteUseCase;
        this.shopPhotoCategoryCreateUseCase = shopPhotoCategoryCreateUseCase;
        this.shopPhotoCategoryUpdateUseCase = shopPhotoCategoryUpdateUseCase;
        this.shopPhotoCategoryDeleteUseCase = shopPhotoCategoryDeleteUseCase;
        this.shopPhotoCategoryImageCreateUseCase = shopPhotoCategoryImageCreateUseCase;
        this.shopPhotoCategoryImageUpdateUseCase = shopPhotoCategoryImageUpdateUseCase;
        this.shopPhotoCategoryImageDeleteUseCase = shopPhotoCategoryImageDeleteUseCase;
        this.shopChoiceCreateUseCase = shopChoiceCreateUseCase;
        this.shopChoiceUpdateUseCase = shopChoiceUpdateUseCase;
        this.shopChoiceDeleteUseCase = shopChoiceDeleteUseCase;
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

    /**
     * 가게를 등록한다. {@code ceoId}를 함께 지정하면 접근권한 부여 이력이 남으므로 조치한 관리자를
     * 인증 주체에서 얻어 함께 넘긴다 — 요청·응답 계약은 변하지 않는다.
     */
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

    /**
     * 가게에 담당 점주를 배정한다. 리소스 등록이 아니라 관계 설정(상태전이)이므로 등록 POST의
     * "생성된 id 반환" 규칙 적용 대상이 아니며 {@code Void}를 반환한다.
     */
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

    /**
     * 가게의 담당 점주 배정을 해제한다. 해제 이후 그 점주의 해당 가게 관리 호출은 전부 403이 된다.
     */
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

    @Operation(summary = "운영시간 목록 조회", description = "가게의 운영시간 목록을 조회합니다.")
    @GetMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<ShopBusinessHourResponse>>> getBusinessHours(@PathVariable Long id) {
        List<ShopBusinessHourResponse> response = shopQueryUseCase.getBusinessHours(id).stream()
            .map(ShopBusinessHourResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "운영시간 등록", description = "가게에 운영시간을 등록합니다.")
    @PostMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<Long>> createBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourManagementCreateCommand command = request.toCreateCommand(userDetails.getPrincipalId(), id);
        Long businessHourId = shopBusinessHourCreateUseCase.createBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(businessHourId));
    }

    @Operation(summary = "운영시간 수정", description = "등록된 운영시간을 수정합니다.")
    @PutMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> updateBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long businessHourId,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourManagementUpdateCommand command = request.toUpdateCommand(userDetails.getPrincipalId(), businessHourId);
        shopBusinessHourUpdateUseCase.updateBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "운영시간 삭제", description = "등록된 운영시간을 삭제합니다.")
    @DeleteMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long businessHourId
    ) {
        ShopBusinessHourManagementDeleteCommand command = ShopBusinessHourManagementDeleteCommand.of(userDetails.getPrincipalId(), businessHourId);
        shopBusinessHourDeleteUseCase.deleteBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 목록 조회", description = "가게의 브레이크타임 목록을 조회합니다.")
    @GetMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<List<ShopBreakTimeResponse>>> getBreakTimes(@PathVariable Long id) {
        List<ShopBreakTimeResponse> response = shopQueryUseCase.getBreakTimes(id).stream()
            .map(ShopBreakTimeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "브레이크타임 등록", description = "가게에 브레이크타임을 등록합니다.")
    @PostMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<Long>> createBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeManagementCreateCommand command = request.toCreateCommand(userDetails.getPrincipalId(), id);
        Long breakTimeId = shopBreakTimeCreateUseCase.createBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(breakTimeId));
    }

    @Operation(summary = "브레이크타임 수정", description = "등록된 브레이크타임을 수정합니다.")
    @PutMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> updateBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long breakTimeId,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeManagementUpdateCommand command = request.toUpdateCommand(userDetails.getPrincipalId(), breakTimeId);
        shopBreakTimeUpdateUseCase.updateBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 삭제", description = "등록된 브레이크타임을 삭제합니다.")
    @DeleteMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long breakTimeId
    ) {
        ShopBreakTimeManagementDeleteCommand command = ShopBreakTimeManagementDeleteCommand.of(userDetails.getPrincipalId(), breakTimeId);
        shopBreakTimeDeleteUseCase.deleteBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "정기 휴무일 목록 조회", description = "가게의 정기 휴무일 목록을 조회합니다.")
    @GetMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<List<ShopClosedDayResponse>>> getClosedDays(@PathVariable Long id) {
        List<ShopClosedDayResponse> response = shopQueryUseCase.getClosedDays(id).stream()
            .map(ShopClosedDayResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "정기 휴무일 등록", description = "가게에 정기 휴무일을 등록합니다.")
    @PostMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<Long>> createClosedDay(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopClosedDaySaveRequest request
    ) {
        ShopClosedDayManagementCreateCommand command = request.toCommand(userDetails.getPrincipalId(), id);
        Long closedDayId = shopClosedDayCreateUseCase.createClosedDay(command);
        return ResponseEntity.ok(ApiResponse.success(closedDayId));
    }

    @Operation(summary = "정기 휴무일 삭제", description = "등록된 정기 휴무일을 삭제합니다.")
    @DeleteMapping("/v1/closed-days/{closedDayId}")
    public ResponseEntity<ApiResponse<Void>> deleteClosedDay(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long closedDayId
    ) {
        ShopClosedDayManagementDeleteCommand command = ShopClosedDayManagementDeleteCommand.of(userDetails.getPrincipalId(), closedDayId);
        shopClosedDayDeleteUseCase.deleteClosedDay(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "편의시설 카테고리 목록 조회", description = "편의시설 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<List<ShopAmenityCategoryResponse>>> getAmenityCategories() {
        List<ShopAmenityCategoryResponse> response = shopQueryUseCase.getAmenityCategories().stream()
            .map(ShopAmenityCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "편의시설 카테고리 등록", description = "편의시설 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<Long>> createAmenityCategory(@Valid @RequestBody ShopAmenityCategoryCreateRequest request) {
        ShopAmenityCategoryCreateCommand command = request.toCommand();
        Long id = shopAmenityCategoryCreateUseCase.createAmenityCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "편의시설 카테고리 수정", description = "편의시설 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/amenity-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateAmenityCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopAmenityCategoryUpdateRequest request
    ) {
        ShopAmenityCategoryUpdateCommand command = request.toCommand(categoryId);
        shopAmenityCategoryUpdateUseCase.updateAmenityCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "음식종류 카테고리 목록 조회", description = "음식종류 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeCategoryResponse>>> getFoodTypeCategories() {
        List<ShopFoodTypeCategoryResponse> response = shopQueryUseCase.getFoodTypeCategories().stream()
            .map(ShopFoodTypeCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "음식종류 카테고리 등록", description = "음식종류 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<Long>> createFoodTypeCategory(@Valid @RequestBody ShopFoodTypeCategoryCreateRequest request) {
        ShopFoodTypeCategoryCreateCommand command = request.toCommand();
        Long id = shopFoodTypeCategoryCreateUseCase.createFoodTypeCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "음식종류 카테고리 수정", description = "음식종류 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/food-type-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateFoodTypeCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopFoodTypeCategoryUpdateRequest request
    ) {
        ShopFoodTypeCategoryUpdateCommand command = request.toCommand(categoryId);
        shopFoodTypeCategoryUpdateUseCase.updateFoodTypeCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 편의시설 목록 조회", description = "가게에 지정된 편의시설 목록을 조회합니다.")
    @GetMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityResponse>>> getShopAmenities(@PathVariable Long id) {
        List<ShopAmenityResponse> response = shopQueryUseCase.getShopAmenities(id).stream()
            .map(ShopAmenityResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 편의시설 지정", description = "가게에 편의시설을 지정합니다.")
    @PostMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<Long>> assignAmenity(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopAmenityAssignRequest request
    ) {
        ShopAmenityManagementAssignCommand command = request.toCommand(userDetails.getPrincipalId(), id);
        Long amenityId = shopAmenityAssignUseCase.assignAmenity(command);
        return ResponseEntity.ok(ApiResponse.success(amenityId));
    }

    @Operation(summary = "가게 편의시설 해제", description = "가게에 지정된 편의시설을 해제합니다.")
    @DeleteMapping("/v1/{id}/amenities/{amenityCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignAmenity(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long amenityCategoryId
    ) {
        ShopAmenityManagementUnassignCommand command = ShopAmenityManagementUnassignCommand.of(userDetails.getPrincipalId(), id, amenityCategoryId);
        shopAmenityUnassignUseCase.unassignAmenity(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 음식종류 목록 조회", description = "가게에 지정된 음식종류 목록을 조회합니다.")
    @GetMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeResponse>>> getShopFoodTypes(@PathVariable Long id) {
        List<ShopFoodTypeResponse> response = shopQueryUseCase.getShopFoodTypes(id).stream()
            .map(ShopFoodTypeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 음식종류 지정", description = "가게에 음식종류를 지정합니다.")
    @PostMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<Long>> assignFoodType(
        @PathVariable Long id,
        @Valid @RequestBody ShopFoodTypeAssignRequest request
    ) {
        ShopFoodTypeAssignCommand command = request.toCommand(id);
        Long foodTypeId = shopFoodTypeAssignUseCase.assignFoodType(command);
        return ResponseEntity.ok(ApiResponse.success(foodTypeId));
    }

    @Operation(summary = "가게 음식종류 해제", description = "가게에 지정된 음식종류를 해제합니다.")
    @DeleteMapping("/v1/{id}/food-types/{foodTypeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignFoodType(
        @PathVariable Long id,
        @PathVariable Long foodTypeCategoryId
    ) {
        ShopFoodTypeUnassignCommand command = ShopFoodTypeUnassignCommand.of(id, foodTypeCategoryId);
        shopFoodTypeUnassignUseCase.unassignFoodType(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "태그 목록 조회", description = "태그 목록을 조회합니다.")
    @GetMapping("/v1/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> response = shopQueryUseCase.getTags().stream()
            .map(TagResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "태그 등록", description = "새로운 태그를 등록합니다.")
    @PostMapping("/v1/tags")
    public ResponseEntity<ApiResponse<Long>> createTag(@Valid @RequestBody TagCreateRequest request) {
        TagCreateCommand command = request.toCommand();
        Long id = tagCreateUseCase.createTag(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "태그 삭제", description = "등록된 태그를 삭제합니다.")
    @DeleteMapping("/v1/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        TagDeleteCommand command = TagDeleteCommand.of(tagId);
        tagDeleteUseCase.deleteTag(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 주문수단 목록 조회", description = "가게에 지정된 주문수단 목록을 조회합니다.")
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<List<ShopOrderMethodItemResponse>>> getOrderMethods(@PathVariable Long id) {
        List<ShopOrderMethodItemResponse> response = shopQueryUseCase.getOrderMethods(id).stream()
            .map(ShopOrderMethodItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 주문수단 지정", description = "가게에 주문수단을 지정합니다.")
    @PostMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<Long>> assignOrderMethod(
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderMethodAssignRequest request
    ) {
        ShopOrderMethodAssignCommand command = request.toCommand(id);
        Long orderMethodId = shopOrderMethodAssignUseCase.assignOrderMethod(command);
        return ResponseEntity.ok(ApiResponse.success(orderMethodId));
    }

    @Operation(summary = "가게 주문수단 해제", description = "가게에 지정된 주문수단을 해제합니다.")
    @DeleteMapping("/v1/{id}/order-methods/{orderMethod}")
    public ResponseEntity<ApiResponse<Void>> unassignOrderMethod(
        @PathVariable Long id,
        @PathVariable String orderMethod
    ) {
        ShopOrderMethodUnassignCommand command = ShopOrderMethodUnassignCommand.of(id, orderMethod);
        shopOrderMethodUnassignUseCase.unassignOrderMethod(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배너 이미지 목록 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerImageItemResponse>>> getBannerImages(@PathVariable Long id) {
        List<ShopBannerImageItemResponse> response = shopQueryUseCase.getBannerImages(id).stream()
            .map(ShopBannerImageItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배너 이미지 등록", description = "가게에 배너 이미지를 등록합니다.")
    @PostMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<Long>> createBannerImage(
        @PathVariable Long id,
        @Valid @RequestBody ShopBannerImageSaveRequest request
    ) {
        ShopBannerImageCreateCommand command = request.toCommand(id);
        Long bannerImageId = shopBannerImageCreateUseCase.createBannerImage(command);
        return ResponseEntity.ok(ApiResponse.success(bannerImageId));
    }

    @Operation(summary = "배너 이미지 삭제", description = "등록된 배너 이미지를 삭제합니다.")
    @DeleteMapping("/v1/banners/{bannerImageId}")
    public ResponseEntity<ApiResponse<Void>> deleteBannerImage(@PathVariable Long bannerImageId) {
        ShopBannerImageDeleteCommand command = ShopBannerImageDeleteCommand.of(bannerImageId);
        shopBannerImageDeleteUseCase.deleteBannerImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 목록 조회", description = "가게의 포토 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getPhotoCategories(@PathVariable Long id) {
        List<ShopPhotoCategoryResponse> response = shopQueryUseCase.getPhotoCategories(id).stream()
            .map(ShopPhotoCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 등록", description = "가게에 포토 카테고리를 등록합니다.")
    @PostMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategory(
        @PathVariable Long id,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        ShopPhotoCategoryCreateCommand command = request.toCreateCommand(id);
        Long categoryId = shopPhotoCategoryCreateUseCase.createPhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(categoryId));
    }

    @Operation(summary = "포토 카테고리 수정", description = "등록된 포토 카테고리를 수정합니다.")
    @PutMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        ShopPhotoCategoryUpdateCommand command = request.toUpdateCommand(categoryId);
        shopPhotoCategoryUpdateUseCase.updatePhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 삭제", description = "등록된 포토 카테고리를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategory(@PathVariable Long categoryId) {
        ShopPhotoCategoryDeleteCommand command = ShopPhotoCategoryDeleteCommand.of(categoryId);
        shopPhotoCategoryDeleteUseCase.deletePhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 목록 조회", description = "포토 카테고리에 속한 이미지 목록을 조회합니다.")
    @GetMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryImageItemResponse>>> getPhotoCategoryImages(@PathVariable Long categoryId) {
        List<ShopPhotoCategoryImageItemResponse> response = shopQueryUseCase.getPhotoCategoryImages(categoryId).stream()
            .map(ShopPhotoCategoryImageItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 이미지 등록", description = "포토 카테고리에 이미지를 등록합니다.")
    @PostMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategoryImage(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        ShopPhotoCategoryImageCreateCommand command = request.toCreateCommand(categoryId);
        Long imageId = shopPhotoCategoryImageCreateUseCase.createPhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }

    @Operation(summary = "포토 카테고리 이미지 수정", description = "등록된 포토 카테고리 이미지를 수정합니다.")
    @PutMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategoryImage(
        @PathVariable Long imageId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        ShopPhotoCategoryImageUpdateCommand command = request.toUpdateCommand(imageId);
        shopPhotoCategoryImageUpdateUseCase.updatePhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 삭제", description = "등록된 포토 카테고리 이미지를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategoryImage(@PathVariable Long imageId) {
        ShopPhotoCategoryImageDeleteCommand command = ShopPhotoCategoryImageDeleteCommand.of(imageId);
        shopPhotoCategoryImageDeleteUseCase.deletePhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
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
