package com.tastyhouse.application.product.port.out;

import java.util.Optional;

/**
 * BBQ 옵션 동기화 대상 조회 포트(batch 전용).
 *
 * <p>ProductQueryPort에서 분리했다 — batch가 쓰는 것은 이 메서드 하나뿐인데 35개짜리 포트를
 * 통째로 보게 두면 batch-application이 web·ceo의 상품 조회 계약까지 컴파일 클래스패스에서
 * 알게 된다. 구현은 여전히 ProductQueryDao 한 클래스가 맡는다.
 */
public interface ProductBbqSyncQueryPort {

    Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget();
}
