package com.tastyhouse.ceoapi.ceo.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateCommand;

/**
 * 자주 쓰는 문구 등록·수정 요청. 등록과 수정이 같은 필드 셋이라 한 record를 공용한다.
 */
@Schema(description = "자주 쓰는 문구 등록·수정 요청")
public record CeoReplyPhraseCreateRequest(

    @Size(max = 50, message = "문구 이름은 50자를 초과할 수 없습니다.")
    @Schema(
        description = "문구 이름(최대 50자). 입력하지 않으면 목록에서 문구 내용의 앞부분이 대신 표시됩니다.",
        example = "감사 인사"
    )
    String name,

    @NotBlank(message = "문구 내용은 필수입니다.")
    @Size(max = 1000, message = "문구 내용은 1000자를 초과할 수 없습니다.")
    @Schema(
        description = "문구 내용(최대 1000자). 금칙어가 포함되면 저장되지 않습니다.",
        example = "소중한 리뷰 감사합니다. 더 좋은 맛으로 보답하겠습니다!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String content
) {

    public CeoReplyPhraseCreateCommand toCommand(Long ceoId) {
        return new CeoReplyPhraseCreateCommand(ceoId, name, content);
    }

    public CeoReplyPhraseUpdateCommand toCommand(Long ceoId, Long replyPhraseId) {
        return new CeoReplyPhraseUpdateCommand(ceoId, replyPhraseId, name, content);
    }
}
