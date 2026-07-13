package com.tastyhouse.adminapi.bug;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.bug.request.BugReportAssignRequest;
import com.tastyhouse.adminapi.bug.request.BugReportClassifyRequest;
import com.tastyhouse.adminapi.bug.request.BugReportSearchRequest;
import com.tastyhouse.adminapi.bug.request.BugReportStatusUpdateRequest;
import com.tastyhouse.adminapi.bug.response.BugReportDetailResponse;
import com.tastyhouse.adminapi.bug.response.BugReportListItemResponse;
import com.tastyhouse.adminapi.bug.response.BugReportPageResponse;

@Tag(name = "BugReport Admin", description = "버그 제보 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bug-reports")
public class BugReportApiController {

    private final BugReportService bugReportService;

    @Operation(summary = "버그 제보 목록 조회", description = "버그 제보 목록을 페이징 조회합니다. title/content는 부분 일치 검색, memberId는 정확 일치합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<BugReportListItemResponse>>> getBugReports(
        @Valid @ModelAttribute BugReportSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        BugReportPageResponse pageResponse = bugReportService.getBugReports(
            search.title(), search.content(), search.memberId(),
            search.status(), search.category(), search.priority(),
            pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "버그 제보 상세 조회", description = "버그 제보 상세를 조회합니다. 첨부 이미지 URL과 제보 회원 정보를 포함합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<BugReportDetailResponse>> getBugReport(@PathVariable Long id) {
        BugReportDetailResponse response = bugReportService.getBugReport(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "버그 제보 처리 상태 변경", description = "처리 상태를 전이합니다(IN_PROGRESS/RESOLVED/REJECTED/ON_HOLD). RESOLVED·REJECTED 시 처리 결과가 기록됩니다.")
    @PatchMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody BugReportStatusUpdateRequest request
    ) {
        bugReportService.changeStatus(id, request.status(), request.answer());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "버그 제보 분류/우선순위 지정", description = "버그 제보의 분류와 우선순위를 지정합니다(트리아지).")
    @PatchMapping("/v1/{id}/classification")
    public ResponseEntity<ApiResponse<Void>> classify(
        @PathVariable Long id,
        @Valid @RequestBody BugReportClassifyRequest request
    ) {
        bugReportService.classify(id, request.category(), request.priority());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "버그 제보 담당자 배정", description = "버그 제보의 처리 담당 관리자를 배정합니다.")
    @PatchMapping("/v1/{id}/assignee")
    public ResponseEntity<ApiResponse<Void>> assign(
        @PathVariable Long id,
        @Valid @RequestBody BugReportAssignRequest request
    ) {
        bugReportService.assign(id, request.assigneeAdminId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
