package com.tastyhouse.adminapi.notice.adapter.in.web.request;

import com.tastyhouse.adminapplication.notice.port.in.NoticeUpdateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공지사항 수정 요청")
public record NoticeUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "서비스 점검 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "공지사항 본문 내용", example = "2026년 1월 1일 00시부터 02시까지 서비스 점검이 진행됩니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {

    public NoticeUpdateCommand toCommand(Long noticeId) {
        return new NoticeUpdateCommand(noticeId, title, content, visible);
    }
}
