package com.tastyhouse.application.rank.port.out;

/**
 * 랭킹 경품 관리 목록·상세 조회 결과 — admin 경품 관리 화면이 소비한다.
 *
 * <p>web용 {@link RankPrizeResult}와 같은 패키지에 공존하고 이름이 충돌하므로 관리 화면 용도를 나타내는
 * {@code Management} 한정어를 유지한다(CLAUDE.md admin 네이밍 규칙). 파일 ID·원본 파일명을 함께
 * 내려주어 관리 화면이 첨부 파일을 식별할 수 있게 한다.
 *
 * <p>조인으로 얻은 저장 경로는 DAO가 {@code FileUrlResolver}로 표시용 URL까지 변환해 담는다.
 */
public record RankPrizeManagementResult(
    Long id,
    Long periodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId,
    String imageFileName,
    String imageUrl
) {
}
