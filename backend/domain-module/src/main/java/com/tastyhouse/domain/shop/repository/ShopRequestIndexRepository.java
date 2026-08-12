package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청 인덱스 write 포트.
 *
 * <p>조회 메서드 2개가 여기 남는 것은 잔류 기준에 맞다 — 둘 다 상태 전이·불변식 경로에서 도메인 모델을
 * 로드한다({@code findById}는 취소·댓글 작성 시 스레드 소유 가게 재검증, {@code findByRequestTypeAnd
 * SourceRequestId}는 원본 전이를 인덱스에 반영할 대상 찾기). 표현 목적 조회는 전부 CQRS query 측
 * {@code ShopRequestQueryDao}가 담당한다.
 */
public interface ShopRequestIndexRepository {

    ShopRequestIndex save(ShopRequestIndex shopRequestIndex);

    Optional<ShopRequestIndex> findById(Long id);

    Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(ShopRequestType requestType, Long sourceRequestId);
}
