package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;

/**
 * PG 원장 write 포트.
 *
 * <p>{@code TossPaymentRecord}는 PG 응답 원본을 그대로 남기는 insert-only 원장이므로 저장만 갖는다
 * (공통 지침 패턴 4). 조회 경로는 없다 — 대조·감사는 DB에서 직접 수행한다.
 */
public interface TossPaymentRecordRepository {

    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
