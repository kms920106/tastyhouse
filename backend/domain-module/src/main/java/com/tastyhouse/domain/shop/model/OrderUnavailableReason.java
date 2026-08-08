package com.tastyhouse.domain.shop.model;

/**
 * 주문가능 상태가 "불가"일 때의 사유.
 *
 * <p>가게 전체 판정과 주문유형별 판정이 같은 사유 집합을 쓴다 — 유형별 상태는 가게 사유를 그대로
 * 물려받거나({@code OUT_OF_BUSINESS_HOURS} 등) 유형 고유 사유
 * ({@link #ORDER_METHOD_NOT_SUPPORTED} · {@link #SUSPENDED})를 갖기 때문이다.
 *
 * <p>{@code from(String)} 정적 팩토리를 두지 않는 것이 {@link OrderMethod}와 다른 점이다 — 이 enum은
 * 서버가 만들어 내려주기만 하고 클라이언트에서 받지 않으므로 역방향 승격 경로가 필요 없다.
 * HTTP 경계로는 {@code name()} 문자열로 노출한다(도메인 enum 경계 규칙).
 *
 * <p><b>{@link #PUBLIC_HOLIDAY_CLOSED}는 현재 발생하지 않는다</b> — 영업상태 판정의 공휴일 입력이
 * {@code false} 고정이기 때문이다(공휴일 캘린더 미연결). 정의만 해 두고 캘린더 일원화 시 살아난다.
 */
public enum OrderUnavailableReason {

    PERMANENTLY_CLOSED("폐업한 가게입니다"),
    HIDDEN("노출정지 상태입니다"),
    SUSPENDED("영업 임시중지 중입니다"),
    PUBLIC_HOLIDAY_CLOSED("공휴일 휴무입니다"),
    TEMPORARILY_CLOSED("임시휴무 기간입니다"),
    REGULAR_CLOSED_DAY("정기휴무일입니다"),
    OUT_OF_BUSINESS_HOURS("영업시간이 아닙니다"),
    BREAK_TIME("휴게시간입니다"),
    ORDER_METHOD_NOT_SUPPORTED("이 가게가 지원하지 않는 주문유형입니다");

    private final String displayName;

    OrderUnavailableReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
