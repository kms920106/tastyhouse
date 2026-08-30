package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

/**
 * 주문 시 안내문구 관리 조회 포트(CQRS query 측 아웃바운드 포트) — 점주·관리자 화면 공용.
 *
 * <p>노출 설정과 무관하게 등록된 문구를 그대로 조회한다. 회원 노출 조회는
 * {@link ShopOrderNoticeQueryPort}가 소유한다.
 *
 * <p>점주 화면과 관리자 화면이 <b>같은 메서드 하나만</b> 쓰므로 소비자별로 쪼개면 두 인터페이스가
 * 완전히 같아진다(규칙 3). 그래서 관리 성격의 계약 하나로 두고 두 앱이 함께 의존한다.
 */
public interface ShopOrderNoticeManagementQueryPort {

    Optional<ShopOrderNoticeResult> findOrderNotice(Long shopId);
}
