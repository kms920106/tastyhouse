package com.tastyhouse.domain.shared.model;

/**
 * 승인 요청류 애그리거트가 공용으로 쓰는 승인 상태.
 *
 * <p>도메인 특화 예외 변환이 필요 없는 범용 enum이라 {@code from(String)} 승격 팩토리는 두지 않는다.
 * 도메인 특화 승인상태(예: 특정 도메인의 상태 변환 실패)가 필요하면 그 도메인 enum에서
 * 이 enum을 감싸거나 별도로 정의한다.
 */
public enum ApprovalStatus {

    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
