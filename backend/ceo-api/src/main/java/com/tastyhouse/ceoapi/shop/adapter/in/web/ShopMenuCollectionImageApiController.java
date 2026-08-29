package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopMenuCollectionImageQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopMenuCollectionImageOrderRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopMenuCollectionImageResponse;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopMenuCollectionImageCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopMenuCollectionImageCreateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopMenuCollectionImageDeleteCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopMenuCollectionImageReorderCommand;

/**
 * 점주 메뉴모음컷 관리 API — 손님이 가게를 열었을 때 가장 먼저, 가장 상단에서 보는 이미지.
 *
 * <p>이미지 <b>등록</b>만 관리자 검수를 거치고, <b>순서 변경·삭제는 즉시 반영</b>된다 — 검수 대상은
 * 새 이미지의 내용이지 배치가 아니다. 등록 정원은 6개이며 대기·반려 건도 그 정원을 차지한다.
 *
 * <p>모든 핸들러가 경로의 {@code shopId}로 소유권을 검증한다. 삭제는 이미지 id가 경로에 있지만 가게
 * 범위 안에서 대상을 찾으므로, 남의 가게 이미지 id는 소유권 검증을 통과했더라도 404로 떨어진다.
 *
 * <p>목록 조회 경로가 web-api의 손님용 엔드포인트와 <b>같은 URL</b>인 것은 의도된 것이다 — 앱은 서로
 * 다른 호스트·포트로 서비스되고, 응답 계약이 달라(점주는 {@code status}·{@code rejectReason} 포함)
 * 각 모듈이 자기 버전을 소유한다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Shop Menu Collection Image", description = "점주 메뉴모음컷 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopMenuCollectionImageApiController {

    private final ShopMenuCollectionImageQueryUseCase shopMenuCollectionImageQueryService;
    private final ShopMenuCollectionImageCommandUseCase shopMenuCollectionImageCommandUseCase;

    public ShopMenuCollectionImageApiController(
        ShopMenuCollectionImageQueryUseCase shopMenuCollectionImageQueryService,
        ShopMenuCollectionImageCommandUseCase shopMenuCollectionImageCommandUseCase
    ) {
        this.shopMenuCollectionImageQueryService = shopMenuCollectionImageQueryService;
        this.shopMenuCollectionImageCommandUseCase = shopMenuCollectionImageCommandUseCase;
    }

    @Operation(summary = "메뉴모음컷 목록 조회",
        description = "표시 순서대로 조회합니다. 검수 대기·반려 건도 상태와 반려 사유를 담아 함께 내려갑니다.")
    @GetMapping("/v1/{id}/menu-collection-images")
    public ResponseEntity<ApiResponse<List<ShopMenuCollectionImageResponse>>> getMenuCollectionImages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopMenuCollectionImageResponse> response =
            shopMenuCollectionImageQueryService.getMenuCollectionImages(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴모음컷 등록",
        description = "JPG/PNG, 15MB 이하, 최소 1280x960 규격만 허용합니다. 가게당 최대 6개이며 대기·반려 건도 "
            + "정원을 차지합니다. 등록 시 검수 대기 상태가 되고, 승인 후 손님 화면에 노출됩니다.")
    @PostMapping(value = "/v1/{id}/menu-collection-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> registerMenuCollectionImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Parameter(description = "메뉴모음컷 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        ShopMenuCollectionImageCreateCommand command = ShopMenuCollectionImageCreateCommand.of(userDetails.getCeoId(), id);
        Long imageId = shopMenuCollectionImageCommandUseCase.registerMenuCollectionImage(command, file);
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }

    @Operation(summary = "메뉴모음컷 순서 변경",
        description = "화면에 보이는 순서대로 이미지 ID 전체를 보냅니다(replace-all). 목록이 최신 상태와 "
            + "일치하지 않으면 거부됩니다. 승인을 거치지 않고 즉시 반영됩니다.")
    @PutMapping("/v1/{id}/menu-collection-images/order")
    public ResponseEntity<ApiResponse<Void>> changeMenuCollectionImageOrder(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopMenuCollectionImageOrderRequest request
    ) {
        ShopMenuCollectionImageReorderCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopMenuCollectionImageCommandUseCase.reorderMenuCollectionImages(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴모음컷 삭제",
        description = "승인을 거치지 않고 즉시 삭제됩니다. 단 최소 1개는 남아야 하므로 마지막 1개는 삭제할 수 "
            + "없습니다. 삭제 후 남은 것의 표시 순서는 서버가 0부터 다시 매깁니다.")
    @DeleteMapping("/v1/{id}/menu-collection-images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCollectionImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long imageId
    ) {
        ShopMenuCollectionImageDeleteCommand command = ShopMenuCollectionImageDeleteCommand.of(userDetails.getCeoId(), id, imageId);
        shopMenuCollectionImageCommandUseCase.deleteMenuCollectionImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
