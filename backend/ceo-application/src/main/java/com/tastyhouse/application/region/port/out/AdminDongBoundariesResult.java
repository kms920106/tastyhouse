package com.tastyhouse.application.region.port.out;

import java.util.List;

/**
 * 행정동 경계 조회 결과 — 생략 여부와 경계 목록.
 *
 * <p><b>챕터 09</b>에서 신설. {@code truncated}는 조회 영역이 임계 면적을 넘었는지에 대한 서비스의
 * 판정 결과이지 DAO가 읽어 오는 값이 아니라, 경계 목록만으로는 응답을 만들 수 없다. 표현 계약이
 * {@code from(Result)} 한 번으로 끝낼 수 있도록 둘을 묶는다.
 */
public record AdminDongBoundariesResult(
    boolean truncated,
    List<AdminDongBoundaryViewResult> items
) {
}
