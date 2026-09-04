package com.tastyhouse.webapi.menureview.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.menureview.port.in.MenuReviewUpdateCommand;

@Schema(description = "메뉴 평가 수정 요청")
public record MenuReviewUpdateRequest(

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "메뉴 평점 (1~5)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer rating,

    @Size(max = 300, message = "코멘트는 300자 이내로 입력해주세요")
    @Schema(description = "짧은 코멘트 (선택)", example = "조금 짰어요")
    String comment
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code menuReviewId}를 주입받아 command로 변환한다.
     */
    public MenuReviewUpdateCommand toCommand(Long memberId, Long menuReviewId) {
        return new MenuReviewUpdateCommand(
            memberId,
            menuReviewId,
            rating,
            comment
        );
    }
}
