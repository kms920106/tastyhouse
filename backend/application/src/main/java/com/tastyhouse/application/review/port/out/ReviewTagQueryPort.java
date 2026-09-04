package com.tastyhouse.application.review.port.out;

import java.util.List;

/**
 * 리뷰 태그 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면과 관리 화면이 함께 쓴다.
 *
 * <p>리뷰에 달린 태그는 두 화면이 같은 형태로 보므로, 소비자별로 쪼개면 두 인터페이스가 사실상
 * 같아진다(규칙 3). 그렇다고 어느 한 앱의 포트에 두면 다른 앱이 그 계약을 주입해야 하는데, 실제로
 * 분할 전 admin이 태그 두 건 때문에 회원 화면용 {@code ReviewQueryPort}를 통째로 주입하고 있었다.
 *
 * <p>그래서 공유분만 별도 포트로 떼어 <b>어느 앱도 남의 계약을 알지 않게</b> 한다. 구현은 태그 조회를
 * 이미 소유한 {@code ReviewQueryDao}가 담당하므로 투영 코드는 복제되지 않는다.
 *
 * <p>선언 중복(규칙 2) 대신 별도 포트를 쓰는 이유는 관리 화면 쪽 계약
 * ({@link ReviewManagementQueryPort})의 구현이 {@code ReviewManagementQueryDao}라 서로 다른 빈이기
 * 때문이다 — 거기에 선언을 중복하면 태그 조회 구현을 그 DAO에도 만들어야 해서 본문이 복제된다.
 */
public interface ReviewTagQueryPort {

    List<Long> findTagIdsByReviewId(Long reviewId);

    List<String> findTagNamesByIds(List<Long> tagIds);
}
