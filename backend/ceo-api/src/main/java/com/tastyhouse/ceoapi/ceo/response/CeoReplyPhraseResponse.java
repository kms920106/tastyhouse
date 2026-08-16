package com.tastyhouse.ceoapi.ceo.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 자주 쓰는 문구 목록 항목 응답.
 *
 * <p>{@code name}과 {@code displayName}을 함께 내려준다 — 전자는 수정 폼에 되돌려 채울 원본이고(비어
 * 있으면 비어 있는 그대로여야 한다), 후자는 목록에 찍을 표시명이다. 둘을 하나로 합치면 이름을 비운 채로
 * 등록한 문구를 수정하려 할 때 파생된 앞부분이 이름 칸에 들어가 그대로 저장되는 사고가 난다.
 */
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

    public static CeoReplyPhraseResponse from(
        Long id,
        String name,
        String displayName,
        String content,
        Integer sort,
        LocalDateTime createdAt
    ) {
        return new CeoReplyPhraseResponse(
            id,
            name,
            displayName,
            content,
            sort,
            createdAt
        );
    }
}
