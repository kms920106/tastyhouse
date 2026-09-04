package com.tastyhouse.adminapi.notice.adapter.in.web.request;

import com.tastyhouse.application.notice.port.in.NoticeCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 생성 요청")
public record NoticeCreateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "서비스 점검 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "공지사항 본문 내용", example = "2026년 1월 1일 00시부터 02시까지 서비스 점검이 진행됩니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @Schema(description = "노출 여부 (미지정 시 노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {

    public NoticeCreateCommand toCommand() {
        return new NoticeCreateCommand(title, content, visible);
    }
}
