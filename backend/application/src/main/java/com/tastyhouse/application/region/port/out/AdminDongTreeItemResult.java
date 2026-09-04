package com.tastyhouse.application.region.port.out;

/**
 * 행정동 계층(트리) 한 단계의 항목.
 *
 * <p>시도·시군구·행정동 세 레벨이 같은 형태를 공유한다 — 화면이 같은 목록 컴포넌트로 3단을 모두 그리기
 * 때문에 레벨마다 다른 타입을 두면 프론트가 분기해야 한다.
 *
 * <p>{@code adminDongId}·{@code code}는 <b>{@code DONG} 레벨에서만</b> 채워진다. 시도·시군구는 이름으로만
 * 존재하는 그룹핑 단위이고 마스터 테이블에 자기 행이 없으므로 식별자가 없다.
 *
 * <p>{@code dongCount}는 그 항목 아래 몇 개의 행정동이 있는지이며, {@code DONG} 레벨에서는 항상 1이다.
 */
public record AdminDongTreeItemResult(
    String name,
    Long adminDongId,
    String code,
    long dongCount
) {
}
