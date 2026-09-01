package com.tastyhouse.webapplication.member.port.out;

/**
 * 내 등급 정보 조회 결과 — 현재 등급·다음 등급·다음 등급까지 남은 리뷰 수.
 *
 * <p><b>챕터 10</b>에서 신설. 리뷰 수만 리포지토리에서 읽고 나머지 다섯 필드는 도메인 enum
 * {@code MemberGrade}의 <b>등급 배정·승급 계산</b>({@code fromReviewCount}·{@code isHigherThanOrEqual}·
 * {@code fromLevel}·{@code getMinReviewCount})에서 파생되므로, 포트 하나의 투영이 아니라 서비스가
 * 만들어 내는 계산 결과다. 따라서 공용 읽기 계약 패키지에 형제로 둘 수 없고 앱 네임스페이스에 둔다
 * (선례: {@code webapplication.grade.port.out.GradeInfoResult}).
 *
 * <p>그 계산이 web-api로 올라가면 안 되는 이유는 규약으로도 강제된다 —
 * {@code apiModuleShouldOnlyReadDomainEnums}가 도메인 enum의 비-accessor 호출을 금지하므로
 * {@code fromReviewCount} 같은 등급 계산은 애초에 api 모듈에서 호출할 수 없다.
 *
 * <p>{@code currentGrade}·{@code nextGrade}는 {@code MemberGrade#name()}으로 강등한 String이다 —
 * 인바운드 포트가 도메인 enum을 노출하지 않게 하려면 강등이 서비스에서 끝나야 한다. 최고 등급이면
 * {@code nextGrade}·{@code nextGradeDisplayName}이 null이고 {@code reviewsNeededForNextGrade}가 0이다.
 */
public record MyGradeResult(
    String currentGrade,
    String currentGradeDisplayName,
    String nextGrade,
    String nextGradeDisplayName,
    int currentReviewCount,
    int reviewsNeededForNextGrade
) {
}
