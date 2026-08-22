package com.tastyhouse.domain.product.port;

/**
 * 통합 요청처리 인덱스({@code SHOP_REQUEST_INDEX})에 상태 전이를 반영하는 출력 포트
 * (product → shop 방향).
 *
 * <p><b>왜 포트인가</b>: 매장 가격 인증 요청은 점주 요청 통합 인덱스에 올라타므로 승인·반려·취소가
 * 그 인덱스와 <b>같은 트랜잭션</b>에서 동기화돼야 한다. 그런데 그 인덱스와 기록자
 * ({@code ShopRequestIndexRecorder})는 shop 컨텍스트 소유이고, 컨텍스트 경계 규칙
 * ({@code ContextBoundaryTest})은 타 컨텍스트의 {@code service}·{@code model} 직접 import를
 * 금지한다. 그래서 product는 이 포트로만 인덱스를 건드린다.
 *
 * <p><b>{@code status}가 도메인 enum이 아니라 {@code String}인 이유</b>도 같은 경계다 —
 * 통합 상태 {@code ShopRequestStatus}는 shop 컨텍스트 소유라 이 포트의 시그니처에 등장할 수 없다.
 * product는 자신의 {@code StorePriceVerificationStatus}를 통합 상태의 <b>상수명 문자열</b>로 옮겨
 * 넘기고, 어댑터가 그 문자열을 shop enum으로 승격한다({@code SocialProfile}이 도메인 enum 대신
 * 상수명 문자열을 나르는 것과 같은 방식).
 *
 * <p>구현은 infrastructure-module의 {@code ShopRequestIndexSyncAdapter}가
 * {@code ShopRequestIndexRecorder}에 위임한다.
 */
public interface ShopRequestIndexSyncPort {

    /**
     * 매장 가격 인증 요청의 상태 전이를 인덱스에 반영한다.
     *
     * @param sourceRequestId 원본 요청 행 ID
     * @param status          통합 상태의 상수명(예: {@code "APPROVED"})
     * @param rejectReason    반려 사유. 반려가 아닌 전이면 {@code null}
     */
    void syncStorePriceVerificationStatus(Long sourceRequestId, String status, String rejectReason);
}
