package com.tastyhouse.ceoapi.shop.adapter.in.web;

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

import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopPhoneNumberCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopPhoneNumberResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberPrimaryDesignateCommand;

@Tag(name = "Ceo Shop Phone Number", description = "점주 가게 전화번호 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopPhoneNumberApiController {

    private final ShopPhoneNumberQueryUseCase shopPhoneNumberQueryService;
    private final ShopPhoneNumberCommandUseCase shopPhoneNumberCommandUseCase;

    public ShopPhoneNumberApiController(ShopPhoneNumberQueryUseCase shopPhoneNumberQueryService, ShopPhoneNumberCommandUseCase shopPhoneNumberCommandUseCase) {
        this.shopPhoneNumberQueryService = shopPhoneNumberQueryService;
        this.shopPhoneNumberCommandUseCase = shopPhoneNumberCommandUseCase;
    }

    @Operation(summary = "내 가게 전화번호 목록 조회", description = "로그인한 점주가 소유한 가게의 전화번호 목록을 조회합니다.")
    @GetMapping("/v1/{id}/phone-numbers")
    public ResponseEntity<ApiResponse<List<ShopPhoneNumberResponse>>> getPhoneNumbers(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopPhoneNumberResponse> response = shopPhoneNumberQueryService.getPhoneNumbers(userDetails.getCeoId(), id).stream()
            .map(ShopPhoneNumberResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 전화번호 등록", description = "로그인한 점주가 소유한 가게에 전화번호를 등록합니다(최대 10개).")
    @PostMapping("/v1/{id}/phone-numbers")
    public ResponseEntity<ApiResponse<Long>> addPhoneNumber(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopPhoneNumberCreateRequest request
    ) {
        ShopPhoneNumberCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long phoneNumberId = shopPhoneNumberCommandUseCase.addPhoneNumber(command);
        return ResponseEntity.ok(ApiResponse.success(phoneNumberId));
    }

    @Operation(summary = "내 가게 전화번호 삭제", description = "로그인한 점주가 소유한 가게의 전화번호를 삭제합니다.")
    @DeleteMapping("/v1/phone-numbers/{phoneNumberId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoneNumber(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long phoneNumberId
    ) {
        ShopPhoneNumberDeleteCommand command = ShopPhoneNumberDeleteCommand.of(userDetails.getCeoId(), phoneNumberId);
        shopPhoneNumberCommandUseCase.deletePhoneNumber(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 대표 전화번호 지정", description = "로그인한 점주가 소유한 가게의 대표 전화번호를 지정합니다.")
    @PatchMapping("/v1/phone-numbers/{phoneNumberId}/primary")
    public ResponseEntity<ApiResponse<Void>> designatePrimary(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long phoneNumberId
    ) {
        ShopPhoneNumberPrimaryDesignateCommand command = ShopPhoneNumberPrimaryDesignateCommand.of(userDetails.getCeoId(), phoneNumberId);
        shopPhoneNumberCommandUseCase.designatePrimary(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
