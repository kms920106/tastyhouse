package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 요청건 담당자 답변 작성 요청.
 */
@Schema(description = "요청건 담당자 답변 작성 요청")
public record ShopRequestCommentCreateRequest(

    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 1000, message = "답변 내용은 1000자 이하여야 합니다.")
    @Schema(
        description = "답변 내용",
        example = "제출하신 동의서의 서명 페이지가 누락되어 있어 재업로드가 필요합니다.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String content
) {

    public ShopRequestCommentManagementCreateCommand toCommand(Long requestId, Long adminId) {
        return new ShopRequestCommentManagementCreateCommand(requestId, adminId, content);
    }
}
