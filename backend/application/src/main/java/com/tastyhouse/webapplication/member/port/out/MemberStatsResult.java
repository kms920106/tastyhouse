package com.tastyhouse.webapplication.member.port.out;

/**
 * 회원 활동 통계 조회 결과 — 리뷰 수·팔로잉 수·팔로워 수.
 *
 * <p><b>챕터 10</b>에서 신설. 이 값은 <b>세 번의 협력자 호출을 모아 만드는 합성 결과</b>라 공용 읽기
 * 계약 패키지({@code com.tastyhouse.application.member.port.out})에 형제로 둘 수 없다 — 그 패키지는
 * 포트 하나의 산출물을 담는 자리이고, 여기서 집계가 일어나는 지점은 application 서비스
 * ({@code MemberStatsQueryService})다. 컨트롤러가 대신 모으게 하면 컨트롤러가 리뷰·팔로우 유스케이스를
 * 각각 알아야 하므로 인바운드 어댑터가 application 흐름을 알게 된다(선례: {@code PointHistoryViewResult}).
 */
public record MemberStatsResult(
    long reviewCount,
    long followingCount,
    long followerCount
) {
}
