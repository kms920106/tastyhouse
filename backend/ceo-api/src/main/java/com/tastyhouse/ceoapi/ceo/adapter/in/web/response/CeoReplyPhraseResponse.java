package com.tastyhouse.ceoapi.ceo.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

@Schema(description = "자주 쓰는 문구 목록 항목")
public record CeoReplyPhraseResponse(

    @Schema(description = "문구 ID", example = "12")
    Long id,

    @Schema(description = "점주가 입력한 문구 이름. 미입력이면 null", example = "감사 인사")
    String name,

    @Schema(
        description = "화면 표시명. 이름이 있으면 그 값, 없으면 내용 앞 20자에 말줄임표를 붙인 값",
        example = "감사 인사"
    )
    String displayName,

    @Schema(description = "문구 내용", example = "소중한 리뷰 감사합니다. 더 좋은 맛으로 보답하겠습니다!")
    String content,

    @Schema(description = "정렬 순서(오름차순)", example = "0")
    Integer sort,

    @Schema(description = "생성 일시", example = "2026-08-14T09:12:41")
    LocalDateTime createdAt
) {
    private static final int DISPLAY_NAME_LENGTH = 20;

    private static final String ELLIPSIS = "…";

    public static CeoReplyPhraseResponse from(CeoReplyPhraseResult result) {
        return new CeoReplyPhraseResponse(
            result.id(),
            result.name(),
            resolveDisplayName(result.name(), result.content()),
            result.content(),
            result.sort(),
            result.createdAt()
        );
    }

    private static String resolveDisplayName(String name, String content) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (content.length() <= DISPLAY_NAME_LENGTH) {
            return content;
        }
        return content.substring(0, DISPLAY_NAME_LENGTH) + ELLIPSIS;
    }
}
