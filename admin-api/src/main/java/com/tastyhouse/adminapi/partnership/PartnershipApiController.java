package com.tastyhouse.adminapi.partnership;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.partnership.request.PartnershipSearchRequest;
import com.tastyhouse.adminapi.partnership.request.PartnershipStatusUpdateRequest;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestDetailResponse;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestListItemResponse;

@Tag(name = "Partnership Admin", description = "제휴 신청 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partnership-requests")
public class PartnershipApiController {

    private final PartnershipCommandService partnershipCommandService;
    private final PartnershipQueryService partnershipQueryService;

    @Operation(summary = "제휴 신청 목록 조회", description = "제휴 신청 목록을 페이징 조회합니다. 상호명/담당자명/연락처/처리상태/접수기간 필터를 지원합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<PartnershipRequestListItemResponse>>> getPartnershipRequests(
        @Valid @ModelAttribute PartnershipSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<PartnershipRequestListItemResponse> pageResponse = partnershipQueryService.getPartnershipRequests(
            search.businessName(), search.contactName(), search.contactPhone(), search.status(),
            search.startDate(), search.endDate(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "제휴 신청 상세 조회", description = "제휴 신청 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<PartnershipRequestDetailResponse>> getPartnershipRequest(@PathVariable Long id) {
        PartnershipRequestDetailResponse response = partnershipQueryService.getPartnershipRequest(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "제휴 신청 처리 상태 변경", description = "제휴 신청의 처리 상태를 변경합니다.")
    @PatchMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody PartnershipStatusUpdateRequest request
    ) {
        partnershipCommandService.changeStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "제휴 신청 삭제", description = "제휴 신청을 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartnershipRequest(@PathVariable Long id) {
        partnershipCommandService.deletePartnershipRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
