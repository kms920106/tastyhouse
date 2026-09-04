package com.tastyhouse.application.member.port.out;

/**
 * 회원 관리 상세 + 프로필 이미지 URL 조합 결과.
 *
 * <p>관리 상세는 {@link MemberManagementDetailResult} 투영 한 번으로 채워지지 않는다 — 프로필 이미지는
 * {@code MemberManagementQueryPort#findProfileImageUrl}가 따로 조회하기 때문이다. 두 조회는 같은 읽기
 * 트랜잭션 안에서 이뤄져야 하므로 {@code MemberQueryService}가 조합해 이 record로 반환한다.
 *
 * <p><b>챕터 06</b>에서 신설 — 컨트롤러가 Response를 조립하려면 두 값이 한 번에 넘어와야 하는데,
 * {@link MemberManagementDetailResult}는 QueryDSL {@code Projections.constructor}가 인자 개수로 찾는
 * 투영 대상이라 필드를 늘릴 수 없다. 프레임워크-프리 record이며 admin만 소비하므로
 * admin-application이 소유한다.
 *
 * <p>{@code profileImageUrl}은 미설정 시 {@code null}이다(기존 {@code orElse(null)} 동작 보존).
 */
public record MemberManagementDetailWithProfileImageResult(
    MemberManagementDetailResult member,
    String profileImageUrl
) {
}
