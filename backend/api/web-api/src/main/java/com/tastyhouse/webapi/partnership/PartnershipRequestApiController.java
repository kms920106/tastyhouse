package com.tastyhouse.webapi.partnership;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.partnership.request.PartnershipRequestCreateRequest;

@RestController
@RequestMapping("/api/partnership-requests")
@Tag(name = "Partnership", description = "광고 및 제휴 API")
public class PartnershipRequestApiController {

    private final PartnershipCommandService partnershipCommandService;

    public PartnershipRequestApiController(PartnershipCommandService partnershipCommandService) {
        this.partnershipCommandService = partnershipCommandService;
    }

    @Operation(summary = "광고 및 제휴 신청", description = "광고 및 제휴를 신청합니다. 상호명, 위치 정보(주소, 상세주소), 성명, 연락처, 상담신청시간을 포함합니다. 생성된 제휴 신청 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createPartnershipRequest(
        @Valid @RequestBody PartnershipRequestCreateRequest request
    ) {
        Long partnershipRequestId = partnershipCommandService.createPartnershipRequest(
            request.businessName(),
            request.address(),
            request.addressDetail(),
            request.contactName(),
            request.contactPhone(),
            request.consultationRequestedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(partnershipRequestId));
    }
}
