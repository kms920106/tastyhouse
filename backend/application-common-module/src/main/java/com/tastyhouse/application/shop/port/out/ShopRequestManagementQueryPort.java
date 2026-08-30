package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

/**
 * 가게 요청 검수 화면 조회 포트(CQRS query 측 아웃바운드 포트) — 관리자용.
 *
 * <p>관리자가 요청 한 건을 열어 본문과 댓글을 확인하는 조회만 담당한다. 점주 화면 조회는
 * {@link ShopRequestQueryPort}가 소유한다.
 *
 * <p>두 메서드 모두 {@link ShopRequestQueryPort}와 함께 쓰는 <b>공유 메서드</b>라 선언만 중복한다.
 * 관리자 계약이 점주 계약의 부분집합이지만 별도 인터페이스로 두는 이유는, 그러지 않으면 admin이
 * 쓰지도 않는 점주 전용 조회 4개까지 아는 계약을 주입해야 하기 때문이다.
 */
public interface ShopRequestManagementQueryPort {

    /** 공유 메서드 — {@link ShopRequestQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ShopRequestDetailResult> findRequestDetail(Long requestId);

    /** 공유 메서드 — {@link ShopRequestQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<ShopRequestCommentResult> findComments(Long requestId);
}
