package com.tastyhouse.application.region.port.out;

import java.util.List;

/**
 * 행정동 경계 원천 데이터를 외부에서 읽어오는 아웃바운드 포트.
 *
 * <p>구현은 infrastructure:crawling의 {@code AdminDongBoundaryClient}가 맡는다. 소비 앱이 batch 하나뿐이라
 * 읽기 계약 소유 규칙(소비 앱 수가 소유 모듈을 정한다)에 따라 batch-application이 소유한다.
 */
public interface AdminDongBoundaryPort {

    List<AdminDongBoundarySource> fetchAll();
}
