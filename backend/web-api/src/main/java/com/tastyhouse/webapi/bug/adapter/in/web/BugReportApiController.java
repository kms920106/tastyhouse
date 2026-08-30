package com.tastyhouse.webapi.bug.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapplication.bug.port.in.BugReportCommandUseCase;
import com.tastyhouse.webapplication.bug.port.in.BugReportCreateCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.bug.adapter.in.web.request.BugReportCreateRequest;
import com.tastyhouse.webapplication.auth.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/bug-reports")
@Tag(name = "BugReport", description = "버그 제보 API")
public class BugReportApiController {

    private final BugReportCommandUseCase bugReportCommandUseCase;

    public BugReportApiController(BugReportCommandUseCase bugReportCommandUseCase) {
        this.bugReportCommandUseCase = bugReportCommandUseCase;
    }

    @Operation(summary = "버그 제보 등록", description = "버그 제보를 등록합니다. 단말기 정보, 제목, 내용, 이미지를 포함할 수 있습니다. 생성된 버그 제보 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createBugReport(
        @Valid @RequestBody BugReportCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        BugReportCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long bugReportId = bugReportCommandUseCase.createBugReport(command);
        return ResponseEntity.ok(ApiResponse.success(bugReportId));
    }
}
