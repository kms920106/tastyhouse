package com.tastyhouse.webapi.bug;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.core.domain.bug.application.BugReportCommandService;
import com.tastyhouse.core.domain.bug.application.dto.command.CreateBugReportCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.webapi.bug.request.BugReportCreateRequest;
import com.tastyhouse.webapi.bug.response.BugReportResponse;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/bug-reports")
@RequiredArgsConstructor
@Tag(name = "BugReport", description = "버그 제보 API")
public class BugReportApiController {

    private final BugReportCommandService bugReportCommandService;

    @Operation(summary = "버그 제보 등록", description = "버그 제보를 등록합니다. 단말기 정보, 제목, 내용, 이미지를 포함할 수 있습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = BugReportResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<BugReportResponse>> createBugReport(
        @Valid @RequestBody BugReportCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        CreateBugReportCommand command = new CreateBugReportCommand(
            userDetails.getMemberId(),
            request.device(),
            request.title(),
            request.content(),
            request.uploadedFileIds()
        );
        BugReportResult result = bugReportCommandService.create(command);
        return ResponseEntity.ok(ApiResponse.success(BugReportResponse.from(result)));
    }
}
