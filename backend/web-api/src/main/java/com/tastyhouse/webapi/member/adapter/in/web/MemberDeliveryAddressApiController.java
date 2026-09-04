package com.tastyhouse.webapi.member.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.member.adapter.in.web.request.MemberDeliveryAddressCreateRequest;
import com.tastyhouse.webapi.member.adapter.in.web.request.MemberDeliveryAddressUpdateRequest;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberDeliveryAddressItemResponse;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressChangeDefaultCommand;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressCommandUseCase;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressCreateCommand;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressDeleteCommand;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressQueryUseCase;
import com.tastyhouse.webapplication.member.port.in.MemberDeliveryAddressUpdateCommand;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Delivery Address", description = "회원 배달 주소록 API")
public class MemberDeliveryAddressApiController {

    private final MemberDeliveryAddressCommandUseCase memberDeliveryAddressCommandUseCase;
    private final MemberDeliveryAddressQueryUseCase memberDeliveryAddressQueryService;

    public MemberDeliveryAddressApiController(
        MemberDeliveryAddressCommandUseCase memberDeliveryAddressCommandUseCase,
        MemberDeliveryAddressQueryUseCase memberDeliveryAddressQueryService
    ) {
        this.memberDeliveryAddressCommandUseCase = memberDeliveryAddressCommandUseCase;
        this.memberDeliveryAddressQueryService = memberDeliveryAddressQueryService;
    }

    @Operation(
        summary = "배달 주소 목록 조회",
        description = "로그인한 회원의 배달 주소 목록을 조회합니다. 기본 배송지가 먼저 노출되며, 행정동 매칭에 실패한 주소는 regionName이 null입니다."
    )
    @GetMapping("/v1/me/delivery-addresses")
    public ResponseEntity<ApiResponse<List<MemberDeliveryAddressItemResponse>>> getMyDeliveryAddresses(
        @CurrentUser MemberUserDetails userDetails
    ) {
        List<MemberDeliveryAddressItemResponse> responses =
            memberDeliveryAddressQueryService.getMyDeliveryAddresses(userDetails.getMemberId())
                .stream()
                .map(MemberDeliveryAddressItemResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(
        summary = "배달 주소 등록",
        description = "배달 주소를 등록합니다. 회원당 최대 10개까지 등록할 수 있으며, 좌표(위도·경도)는 필수입니다. "
                      + "isDefault가 true면 기존 기본 배송지는 자동으로 해제됩니다. "
                      + "행정동은 서버가 주소 문자열로 매칭해 채우며, 매칭에 실패해도 등록은 성공합니다."
    )
    @PostMapping("/v1/me/delivery-addresses")
    public ResponseEntity<ApiResponse<Long>> createDeliveryAddress(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @RequestBody MemberDeliveryAddressCreateRequest request
    ) {
        MemberDeliveryAddressCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long addressId = memberDeliveryAddressCommandUseCase.createDeliveryAddress(command);
        return ResponseEntity.ok(ApiResponse.success(addressId));
    }

    @Operation(
        summary = "배달 주소 수정",
        description = "본인의 배달 주소를 수정합니다. 주소가 바뀌면 행정동도 다시 매칭합니다. "
                      + "기본 배송지 지정은 별도 엔드포인트(PATCH .../default)를 사용합니다."
    )
    @PutMapping("/v1/me/delivery-addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDeliveryAddress(
        @CurrentUser MemberUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody MemberDeliveryAddressUpdateRequest request
    ) {
        MemberDeliveryAddressUpdateCommand command = request.toCommand(userDetails.getMemberId(), id);
        memberDeliveryAddressCommandUseCase.updateDeliveryAddress(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배달 주소 삭제", description = "본인의 배달 주소를 삭제합니다.")
    @DeleteMapping("/v1/me/delivery-addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryAddress(
        @CurrentUser MemberUserDetails userDetails,
        @PathVariable Long id
    ) {
        MemberDeliveryAddressDeleteCommand command = MemberDeliveryAddressDeleteCommand.of(userDetails.getMemberId(), id);
        memberDeliveryAddressCommandUseCase.deleteDeliveryAddress(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "기본 배달 주소 지정",
        description = "본인의 배달 주소를 기본 배송지로 지정합니다. 기존 기본 배송지는 자동으로 해제되어 회원당 1건만 유지됩니다."
    )
    @PatchMapping("/v1/me/delivery-addresses/{id}/default")
    public ResponseEntity<ApiResponse<Void>> changeDefaultDeliveryAddress(
        @CurrentUser MemberUserDetails userDetails,
        @PathVariable Long id
    ) {
        MemberDeliveryAddressChangeDefaultCommand command = MemberDeliveryAddressChangeDefaultCommand.of(userDetails.getMemberId(), id);
        memberDeliveryAddressCommandUseCase.changeDefaultDeliveryAddress(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
