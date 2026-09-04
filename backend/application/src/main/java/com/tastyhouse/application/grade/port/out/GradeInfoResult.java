package com.tastyhouse.application.grade.port.out;

/**
 * 등급 세부 조건 항목 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 이 컨텍스트는 DB를 읽지 않는다 — 등급 정책이 도메인 enum
 * {@code MemberGrade} 상수에서 파생되는 정적 목록이라 {@code GradeQueryPort}가 없고, 따라서 공용
 * 읽기 계약 패키지({@code com.tastyhouse.application.grade.port.out})에 형제가 없다. 그래도 챕터 10이
 * Response를 web-api로 올리면서 유스케이스 반환 타입이 필요해졌으므로 앱 네임스페이스에 둔다
 * (선례: {@code ceoapplication..port.out}의 Command 반환 Result들).
 *
 * <p>{@code grade}는 {@code MemberGrade#name()}으로 강등한 String이다 — 인바운드 포트가 도메인 enum을
 * 노출하지 않게 하려면 강등이 서비스에서 끝나야 한다.
 */
public record GradeInfoResult(
    String grade,
    String displayName,
    int minReviewCount,
    Integer maxReviewCount
) {
}
