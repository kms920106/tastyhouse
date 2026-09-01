package com.tastyhouse.application.region.port.out;

import java.util.List;

/**
 * 행정동 계층 한 단계의 조회 결과 — 단계 이름과 그 단계의 항목 목록.
 *
 * <p><b>챕터 09</b>에서 신설. {@code level}은 DAO가 읽어 오는 값이 아니라 어느 조회 분기를 탔는지에 따라
 * 서비스가 정하는 값이라, 항목 목록({@link AdminDongTreeItemResult})만으로는 응답을 만들 수 없다.
 * 표현 계약이 {@code from(Result)} 한 번으로 끝낼 수 있도록 둘을 묶는다.
 */
public record AdminDongTreeResult(
    String level,
    List<AdminDongTreeItemResult> items
) {
}
