package com.tastyhouse.ceoapi.product;

import java.time.LocalTime;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.product.request.ProductExposureHourRequest;
import com.tastyhouse.ceoapi.product.request.ProductExposureRequest;
import com.tastyhouse.ceoapi.product.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.response.ProductExposureResponse;

/**
 * 점주 메뉴 노출기간 관리 API.
 *
 * <p>노출 = {@code visible} AND 기간 AND 요일·시간대다. 품절은 직교하므로 이 API가 다루지 않는다 —
 * 품절 메뉴는 목록에 남은 채 '품절' 뱃지만 붙는다.
 *
 * <p>기간·요일·시간대는 <b>replace-all</b>(PUT)로만 바꾼다 — 요일 묶음과 개별 요일의 혼용 금지가
 * 집합 전체를 봐야 판정되는 규칙이라, 행 단위 CRUD를 열면 중간 상태가 반드시 규칙을 위반한다.
 *
 * <p>모든 핸들러가 body 또는 query의 {@code shopId}로 소유권을 검증한다.
 * 역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당한다.
 */
@Tag(name = "Ceo Product Exposure", description = "점주 메뉴 노출기간 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductExposureApiController {

    private final ProductExposureQueryService productExposureQueryService;
    private final ProductExposureCommandService productExposureCommandService;

    public ProductExposureApiController(
        ProductExposureQueryService productExposureQueryService,
        ProductExposureCommandService productExposureCommandService
    ) {
        this.productExposureQueryService = productExposureQueryService;
        this.productExposureCommandService = productExposureCommandService;
    }

    @Operation(summary = "메뉴 노출기간 조회",
        description = "설정된 기간·요일·시간대와 함께 지금 노출 중인지(exposedNow)와 그 사유(hiddenReason)를 반환합니다.")
    @GetMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<ProductExposureResponse>> getProductExposure(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductExposureResponse response = productExposureQueryService.getExposure(
            userDetails.getCeoId(), request.shopId(), id
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 노출기간 설정",
        description = "기간과 요일·시간대를 통째로 치환합니다. hours를 빈 배열로 보내면 요일·시간 제약이 사라집니다.")
    @PutMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<Void>> changeProductExposure(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductExposureRequest request
    ) {
        productExposureCommandService.replaceExposure(
            userDetails.getCeoId(), request.shopId(), id,
            request.startDate(), request.endDate(),
            dayTypes(request.hours()), startTimes(request.hours()), endTimes(request.hours())
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 노출기간 해제",
        description = "기간·요일·시간대를 모두 비워 상시 노출로 되돌립니다. 숨김 상태는 바뀌지 않습니다.")
    @DeleteMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<Void>> clearProductExposure(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        productExposureCommandService.clearExposure(userDetails.getCeoId(), request.shopId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── 변환 ────────────────────────────────────────────────────────────────────────

    /**
     * 요청 record를 원시 필드 병렬 목록으로 언패킹한다 — 세 목록을 <b>같은 순서</b>로 넘긴다.
     *
     * <p>컨트롤러가 {@code DayType}으로 승격하지 않는 이유: 컨트롤러는 {@code com.tastyhouse.domain..}를
     * import하지 않는다(ArchUnit {@code LayerRulesTest}). 승격은 command service가 담당한다.
     */
    private List<String> dayTypes(List<ProductExposureHourRequest> hours) {
        return hours.stream()
            .map(ProductExposureHourRequest::dayType)
            .toList();
    }

    private List<LocalTime> startTimes(List<ProductExposureHourRequest> hours) {
        return hours.stream()
            .map(ProductExposureHourRequest::startTime)
            .toList();
    }

    private List<LocalTime> endTimes(List<ProductExposureHourRequest> hours) {
        return hours.stream()
            .map(ProductExposureHourRequest::endTime)
            .toList();
    }
}
