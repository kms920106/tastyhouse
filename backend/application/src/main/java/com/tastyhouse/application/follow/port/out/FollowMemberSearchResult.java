package com.tastyhouse.application.follow.port.out;

/**
 * 닉네임 검색 결과 항목 — 검색된 회원 요약에 <b>뷰어의 팔로우 여부</b>를 얹은 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 패키지({@code com.tastyhouse.application.member.port.out})의
 * {@code MemberWithProfileImageResult}만으로는 이 응답을 만들 수 없다 — 검색 자체는 회원 읽기 포트가
 * 답하지만 {@code following}은 그 포트의 투영에 없고, 팔로우 읽기 포트를 항목마다 되물어 <b>서비스가
 * 합성</b>하는 파생값이다. 두 포트에 걸친 합성 결과라 어느 포트의 계약 패키지에도 형제가 될 수 없으므로
 * 앱 네임스페이스에 둔다(선례: {@code webapplication.grade.port.out.GradeInfoResult}).
 *
 * <p>{@code grade}는 {@code MemberGrade#name()}으로 강등한 String이다 — 인바운드 포트가 도메인 enum을
 * 노출하지 않게 하려면 강등이 서비스에서 끝나야 한다.
 *
 * <p>{@code profileImageUrl}은 DAO가 표시용 URL까지 변환해 담은 값을 그대로 옮긴 것이다.
 */
public record FollowMemberSearchResult(
    Long memberId,
    String nickname,
    String grade,
    String profileImageUrl,
    boolean following
) {
}
