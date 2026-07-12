package com.tastyhouse.webapi.partnership;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.partnership.request.PartnershipRequestCreateRequest;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;

@RestController
@RequestMapping("/api/partnership-requests")
@RequiredArgsConstructor
@Tag(name = "Partnership", description = "광고 및 제휴 API")
public class PartnershipRequestApiController {

    private final PartnershipRequestService partnershipRequestService;

    @Operation(summary = "광고 및 제휴 신청", description = "광고 및 제휴를 신청합니다. 상호명, 위치 정보(주소, 상세주소), 성명, 연락처, 상담신청시간을 포함합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<PartnershipRequestResponse>> createPartnershipRequest(
        @Valid @RequestBody PartnershipRequestCreateRequest request
    ) {
        PartnershipRequestResponse response = partnershipRequestService.createPartnershipRequest(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
