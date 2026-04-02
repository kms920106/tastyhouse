package com.tastyhouse.webapi.partnership;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.partnership.request.PartnershipRequestCreateRequest;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partnership-requests")
@RequiredArgsConstructor
@Tag(name = "Partnership", description = "광고 및 제휴 API")
public class PartnershipRequestApiController {

    private final PartnershipRequestService partnershipRequestService;

    @Operation(summary = "광고 및 제휴 신청", description = "광고 및 제휴를 신청합니다. 상호명, 위치 정보(주소, 상세주소), 성명, 연락처, 상담신청시간을 포함합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "신청 성공", content = @Content(schema = @Schema(implementation = PartnershipRequestResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PostMapping("/v1")
    public ResponseEntity<CommonResponse<PartnershipRequestResponse>> createPartnershipRequest(
        @Valid @RequestBody PartnershipRequestCreateRequest request
    ) {
        PartnershipRequestResponse response = partnershipRequestService.createPartnershipRequest(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
