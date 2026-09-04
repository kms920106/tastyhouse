package com.tastyhouse.application.rank.port.out;

/**
 * 랭킹 경품 관리 목록·상세 조회 결과 — admin 경품 관리 화면이 소비한다.
 *
 * <p>web용 {@code RankPrizeResult}와 패키지 경로는 같으나 다른 모듈에 있으며, 역할을 구분하기 위해 관리 화면 용도를 나타내는
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
