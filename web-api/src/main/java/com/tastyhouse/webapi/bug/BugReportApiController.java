package com.tastyhouse.webapi.bug;

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
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.bug.request.BugReportCreateRequest;
import com.tastyhouse.webapi.bug.response.BugReportResponse;

@RestController
@RequestMapping("/api/bug-reports")
@RequiredArgsConstructor
@Tag(name = "BugReport", description = "버그 제보 API")
public class BugReportApiController {

    private final BugReportCommandService bugReportCommandService;
    private final BugReportQueryService bugReportQueryService;

    @Operation(summary = "버그 제보 등록", description = "버그 제보를 등록합니다. 단말기 정보, 제목, 내용, 이미지를 포함할 수 있습니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<BugReportResponse>> createBugReport(
        @Valid @RequestBody BugReportCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long bugReportId = bugReportCommandService.createBugReport(
            userDetails.getMemberId(),
            request.device(),
            request.title(),
            request.content(),
            request.appVersion(),
            request.platform(),
            request.osVersion(),
            request.uploadedFileIds()
        );
        BugReportResponse response = bugReportQueryService.getBugReportResponse(bugReportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
