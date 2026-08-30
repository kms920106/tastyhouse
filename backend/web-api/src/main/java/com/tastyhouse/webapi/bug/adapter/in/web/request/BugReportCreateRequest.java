package com.tastyhouse.webapi.bug.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.bug.port.in.BugReportCreateCommand;

@Schema(description = "버그 제보 요청")
public record BugReportCreateRequest(
    @NotBlank(message = "단말기 정보는 필수입니다")
    @Size(max = 100, message = "단말기 정보는 100자 이내로 입력해주세요")
    @Schema(description = "단말기 정보", example = "iPhone 15 Pro / iOS 17.4", requiredMode = Schema.RequiredMode.REQUIRED)
    String device,

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    @Schema(description = "제목", example = "결제 완료 후 앱이 강제 종료됩니다", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "내용은 필수입니다")
    @Schema(description = "상세 내용", example = "결제 승인 버튼을 누르면 앱이 꺼집니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @Size(max = 30, message = "앱 버전은 30자 이내로 입력해주세요")
    @Schema(description = "앱 버전 (선택)", example = "3.2.0")
    String appVersion,

    @Schema(description = "플랫폼 (선택)", example = "IOS", allowableValues = {"IOS", "ANDROID"})
    String platform,

    @Size(max = 30, message = "OS 버전은 30자 이내로 입력해주세요")
    @Schema(description = "OS 버전 (선택)", example = "17.4")
    String osVersion,

    @Size(max = 5, message = "사진은 최대 5장까지 첨부할 수 있습니다")
    @Schema(description = "첨부 이미지 파일 ID 목록 (최대 5장)", example = "[1, 2, 3]")
    List<Long> uploadedFileIds
) {

    public BugReportCreateCommand toCommand(Long reporterId) {
        return new BugReportCreateCommand(reporterId, device, title, content, appVersion, platform, osVersion, uploadedFileIds);
    }
}
