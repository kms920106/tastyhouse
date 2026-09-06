package com.tastyhouse.domain.product.model;

/**
 * 메뉴가 손님 메뉴판에서 보이지 않는 사유.
 *
 * <p><b>DB에 저장되지 않는다</b> — {@code ProductExposureCalculator}가 판정한 결과를 화면에
 * 설명하기 위한 값이므로 {@code @Enumerated}·{@code columnDefinition} 규칙과 무관하다.
 *
 * <p>품절({@code soldOut})은 여기 없다. 품절은 노출 축과 <b>직교</b>하며 목록에 남은 채
 * '품절' 뱃지만 붙는다 — 스케줄과 같은 축에 두면 시간 밖 메뉴가 '품절'로 표시되는 잘못된 UX가 된다.
 */
public enum ProductHiddenReason {

    /** 점주가 직접 숨긴 상태. 점주의 명시적 의사가 스케줄을 이긴다. */
    MANUALLY_HIDDEN("점주가 숨김 처리한 메뉴입니다."),

    /** 노출 시작일 이전. */
    BEFORE_EXPOSURE_PERIOD("노출 시작일 이전입니다."),

    /** 노출 종료일 이후(종료일 당일은 포함하므로 그 다음 날부터). */
    AFTER_EXPOSURE_PERIOD("노출 종료일이 지났습니다."),

    /** 설정된 요일·시간대 밖. */
    OUT_OF_EXPOSURE_HOURS("노출 시간대가 아닙니다.");

    private final String description;

    ProductHiddenReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
