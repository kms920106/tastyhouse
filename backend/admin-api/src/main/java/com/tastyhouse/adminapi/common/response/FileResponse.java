package com.tastyhouse.adminapi.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 업로드 파일 정보 응답(중첩 표현 계약).
 *
 * <p>배너·이벤트·버그리포트·랭크 4개 컨텍스트의 Response에 중첩되므로 특정 컨텍스트가 아니라
 * {@code common}에 둔다(챕터 06). 각 컨텍스트의 {@code *Result}는 파일 식별자·파일명·URL을 평면으로
 * 투영하므로, 컨트롤러가 그 세 값으로 이 record를 조립한다 — 파일을 다시 조회하지 않는다.
 */
@Schema(description = "업로드 파일 정보")
public record FileResponse(
    @Schema(description = "파일 ID", example = "1")
    Long id,

    @Schema(description = "원본 파일명", example = "summer-promotion.png")
    String name,

    @Schema(description = "파일 접근 URL", example = "https://cdn.tastyhouse.com/banner/1.png")
    String url
) {
    public static FileResponse of(
        Long id,
        String name,
        String url
    ) {
        return new FileResponse(
            id,
            name,
            url
        );
    }
}
