package com.tastyhouse.webapplication.point.port.out;

import java.time.LocalDate;

/**
 * 내 포인트 내역 항목의 <b>표시용</b> 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 패키지({@code com.tastyhouse.application.point.port.out})의
 * {@link com.tastyhouse.application.point.port.out.PointHistoryResult}를 그대로 쓸 수 없어 앱
 * 네임스페이스에 둔다(선례: {@code GradeInfoResult}, {@code ceoapplication..port.out}의 Result들).
 * 이유는 두 가지다.
 *
 * <ul>
 *   <li>{@code pointAmount}가 <b>DB 값이 아니라 계산 결과</b>다 — 사용(USE) 내역은 저장된 양수 금액을
 *       음수로 뒤집어 내려보낸다. 이 부호 규칙은 표현 규칙이 아니라 회원 화면의 도메인 규칙이므로
 *       application 계층에 남아야 하고, 컨트롤러나 Response가 흉내낼 수 없다.
 *   <li>{@code date}가 원본 {@code createdAt}({@code LocalDateTime})을 날짜로 절단한 값이다.
 * </ul>
 *
 * <p>{@code pointType}은 도메인 enum {@code PointType#name()}으로 강등한 String이다 — 인바운드 포트가
 * 도메인 enum을 노출하지 않게 하려면 강등이 서비스에서 끝나야 하고, 무엇보다 위 부호 판정이 이 강등된
 * 문자열({@code "USE"})을 기준으로 이뤄진다.
 */
public record PointHistoryItemViewResult(
    String reason,
    LocalDate date,
    Integer pointAmount,
    String pointType
) {
}
