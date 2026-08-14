package com.tastyhouse.domain.ceo.model;

/**
 * 점주 로그인 실패 사유.
 *
 * <p>존재하지 않는 아이디는 이 목록에 없다 — 귀속할 점주가 없어 어떤 점주의 접속기록에도 속하지 않고,
 * 임의 문자열이 쌓이면 계정 존재 여부를 탐색하는 표면이 되므로 아예 기록하지 않는다.
 *
 * <p>{@code from(String)}을 두지 않는다 — 조회 필터는 {@code result}(성공/실패)까지만이고, 실패 사유로
 * 좁히는 화면이 없어 HTTP 경계를 넘어오지 않는다.
 */
public enum CeoLoginFailureReason {

    BAD_CREDENTIALS("비밀번호 불일치"),
    ACCOUNT_INACTIVE("비활성 계정");

    private final String description;

    CeoLoginFailureReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
