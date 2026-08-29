package com.tastyhouse.application.event.port.out;

/**
 * 이벤트 상세 조회 결과(web) — 배너 이미지 표시용 URL만 노출한다.
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. admin
 * 관리 상세용 형제({@link EventManagementDetailResult})와 같은 패키지에 공존하므로, 관리 화면 쪽이
 * {@code Management} 한정어를 유지해 이름 충돌을 피한다.
 *
 * <p>조인으로 얻은 저장 경로는 DAO가 {@code FileUrlResolver}로 표시용 URL까지 변환해 담는다.
 */
public record EventDetailResult(
    String bannerUrl
) {
}
