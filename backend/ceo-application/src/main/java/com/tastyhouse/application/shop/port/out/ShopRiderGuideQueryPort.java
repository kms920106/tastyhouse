package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

/**
 * 라이더 안내 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 화면용.
 *
 * <p>점주가 자기 가게에 등록한 라이더 안내를 확인한다. 전체 가게를 검수하는 관리 화면 조회는
 * {@link ShopRiderGuideManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findRiderGuide}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface ShopRiderGuideQueryPort {

    /** 공유 메서드 — {@link ShopRiderGuideManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ShopRiderGuideResult> findRiderGuide(Long shopId);
}
