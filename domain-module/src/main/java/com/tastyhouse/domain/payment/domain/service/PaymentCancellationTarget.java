package com.tastyhouse.domain.payment.domain.service;

import com.tastyhouse.domain.payment.domain.model.PaymentCancelCode;

/**
 * 취소 대상(사전 판정 결과).
 *
 * <p>PG 취소 요청을 DB 트랜잭션 밖으로 빼면서 결제 취소가 "사전 판정(트랜잭션 1) → PG 취소 요청(트랜잭션
 * 없음) → 결과 반영(트랜잭션 2)" 3단으로 쪼개졌다. 이 record는 1단의 판정 결과를 트랜잭션 밖으로 실어
 * 나른다.
 *
 * <p>두 가지 상태만 갖는다.
 * <ul>
 *   <li><b>거절</b>({@link #rejected}) — 주문 상태 때문에 취소할 수 없다. {@code rejectCode}가 사유이며
 *       호출자는 PG를 호출하지 않고 이 코드를 그대로 반환한다.</li>
 *   <li><b>취소 가능</b>({@link #cancellable}) — {@code pgCancelRequired}가 참이면 호출자가 {@code pgTid}로
 *       PG 취소를 먼저 요청하고, 거짓이면(미승인 결제 등) PG 호출 없이 바로 2단으로 넘어간다.</li>
 * </ul>
 *
 * <p>도메인 모델({@code Payment}/{@code Order})을 담지 않는 이유는 {@link TossConfirmationTarget}과 같다 —
 * 트랜잭션이 끝난 뒤의 detached 애그리거트를 트랜잭션 밖으로 내보내지 않고, 2단이 새 트랜잭션에서 다시
 * 로드하도록 한다.
 */
public record PaymentCancellationTarget(
    PaymentCancelCode rejectCode,
    boolean pgCancelRequired,
    String pgTid
) {

    public static PaymentCancellationTarget rejected(PaymentCancelCode rejectCode) {
        return new PaymentCancellationTarget(
            rejectCode,
            false,
            null
        );
    }

    public static PaymentCancellationTarget cancellable(boolean pgCancelRequired, String pgTid) {
        return new PaymentCancellationTarget(
            null,
            pgCancelRequired,
            pgTid
        );
    }

    /**
     * 주문 상태 때문에 취소가 거절되었는지 — 참이면 {@link #rejectCode}를 그대로 응답한다.
     */
    public boolean isRejected() {
        return rejectCode != null;
    }
}
