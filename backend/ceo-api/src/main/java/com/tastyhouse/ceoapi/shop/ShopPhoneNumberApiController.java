package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopPhoneNumberCreateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopPhoneNumberResponse;

@Tag(name = "Ceo Shop Phone Number", description = "점주 가게 전화번호 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopPhoneNumberApiController {

    private final ShopPhoneNumberQueryService shopPhoneNumberQueryService;
    private final ShopPhoneNumberCommandService shopPhoneNumberCommandService;

    public ShopPhoneNumberApiController(ShopPhoneNumberQueryService shopPhoneNumberQueryService, ShopPhoneNumberCommandService shopPhoneNumberCommandService) {
        this.shopPhoneNumberQueryService = shopPhoneNumberQueryService;
        this.shopPhoneNumberCommandService = shopPhoneNumberCommandService;
    }

    @Operation(summary = "내 가게 전화번호 목록 조회", description = "로그인한 점주가 소유한 가게의 전화번호 목록을 조회합니다.")
    @GetMapping("/v1/{id}/phone-numbers")
    public ResponseEntity<ApiResponse<List<ShopPhoneNumberResponse>>> getPhoneNumbers(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopPhoneNumberResponse> response = shopPhoneNumberQueryService.getPhoneNumbers(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 전화번호 등록", description = "로그인한 점주가 소유한 가게에 전화번호를 등록합니다(최대 10개).")
    @PostMapping("/v1/{id}/phone-numbers")
    public ResponseEntity<ApiResponse<Long>> addPhoneNumber(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopPhoneNumberCreateRequest request
    ) {
        Long phoneNumberId = shopPhoneNumberCommandService.addPhoneNumber(userDetails.getCeoId(), id, request.phoneNumber(), request.virtual());
        return ResponseEntity.ok(ApiResponse.success(phoneNumberId));
    }

    @Operation(summary = "내 가게 전화번호 삭제", description = "로그인한 점주가 소유한 가게의 전화번호를 삭제합니다.")
    @DeleteMapping("/v1/phone-numbers/{phoneNumberId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoneNumber(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long phoneNumberId
    ) {
        shopPhoneNumberCommandService.deletePhoneNumber(userDetails.getCeoId(), phoneNumberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 대표 전화번호 지정", description = "로그인한 점주가 소유한 가게의 대표 전화번호를 지정합니다.")
    @PatchMapping("/v1/phone-numbers/{phoneNumberId}/primary")
    public ResponseEntity<ApiResponse<Void>> designatePrimary(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long phoneNumberId
    ) {
        shopPhoneNumberCommandService.designatePrimary(userDetails.getCeoId(), phoneNumberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
