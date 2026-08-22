package com.tastyhouse.ceoapi.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.response.ShopStorePriceVerificationResponse;

/**
 * 점주 매장 가격 인증 관리 API — 손님에게 '매장과 같은 가격' 뱃지를 보여줄 자격을 얻는 절차다.
 *
 * <p><b>등록이 multipart인 것은 가격표 이미지와 대상 목록이 한 요청에 함께 와야 하기 때문</b>이다.
 * 두 요청으로 쪼개면 중간에서 끊긴 건이 첨부만 있고 대상이 없는 고아 상태로 남아, 관리자 검수 큐에
 * 검수할 수 없는 건이 쌓인다. multipart는 JSON 바디를 함께 실을 수 없어 대상 목록만
 * {@code items} 문자열 파트로 받고 서비스가 파싱한다.
 *
 * <p><b>조회는 {@code verified}와 {@code status}를 함께 내려주며 둘은 서로 다른 축이다.</b> 승인 후에도
 * 배달가가 매장가를 넘어서면 인증이 자동 해제되므로, 최근 요청이 승인인데 인증이 꺼진 상태가 정상적으로
 * 존재한다. 화면은 매장가·픽업가 입력 가능 여부를 {@code verified}로 판단한다.
 *
 * <p>요청 취소·관리자 검수(승인·반려)는 이 컨트롤러에 없다 — 취소는 통합 요청처리 현황
 * ({@code ShopRequestApiController})이, 검수는 admin-api가 담당한다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Shop Store Price Verification", description = "점주 매장 가격 인증 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopStorePriceVerificationApiController {

    private final ShopStorePriceVerificationQueryService shopStorePriceVerificationQueryService;
    private final ShopStorePriceVerificationCommandService shopStorePriceVerificationCommandService;

    public ShopStorePriceVerificationApiController(
        ShopStorePriceVerificationQueryService shopStorePriceVerificationQueryService,
        ShopStorePriceVerificationCommandService shopStorePriceVerificationCommandService
    ) {
        this.shopStorePriceVerificationQueryService = shopStorePriceVerificationQueryService;
        this.shopStorePriceVerificationCommandService = shopStorePriceVerificationCommandService;
    }

    @Operation(summary = "매장 가격 인증 요청",
        description = "가격표 이미지(file)와 인증 대상 목록(items, JSON 문자열)을 함께 보냅니다. "
            + "이미지는 JPG/PNG, 15MB 이하, 최소 750x350 규격만 허용합니다. items의 각 항목은 "
            + "{ productId, priceId, storePrice, applyPickupSamePrice } 형태이며 1건 이상이어야 합니다. "
            + "매장가는 요청 시점 값으로 박제되어 승인 시 그대로 반영됩니다. 검수 대기·진행 중인 요청이 "
            + "있거나 할인이 진행 중인 메뉴가 포함되면 거부됩니다.")
    @PostMapping(value = "/v1/{id}/store-price-verifications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> requestStorePriceVerification(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Parameter(description = "매장 가격표 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file,
        @Parameter(description = "인증 대상 목록(JSON 배열 문자열)", required = true,
            example = "[{\"productId\":1,\"priceId\":10,\"storePrice\":14000,\"applyPickupSamePrice\":true}]")
        @RequestParam("items") String items
    ) {
        Long verificationId = shopStorePriceVerificationCommandService.requestVerification(
            userDetails.getCeoId(), id, file, items
        );
        return ResponseEntity.ok(ApiResponse.success(verificationId));
    }

    @Operation(summary = "매장 가격 인증 현황 조회",
        description = "최근 요청 1건의 상태·반려 사유와 현재 인증 여부(verified), 인증을 충족하지 못한 메뉴 "
            + "목록을 함께 내려줍니다. 한 번도 요청하지 않은 가게는 404가 아니라 id·status·rejectReason이 "
            + "null인 응답이며 verified만 유효합니다. verified와 status는 서로 다른 축이므로(승인 후에도 "
            + "배달가가 매장가를 넘으면 해제됩니다) 매장가 입력 가능 여부는 verified로 판단합니다.")
    @GetMapping("/v1/{id}/store-price-verifications/latest")
    public ResponseEntity<ApiResponse<ShopStorePriceVerificationResponse>> getLatestStorePriceVerification(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopStorePriceVerificationResponse response =
            shopStorePriceVerificationQueryService.getLatestVerification(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
