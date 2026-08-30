package com.tastyhouse.webapplication.grade.port.in;

import java.util.List;

import com.tastyhouse.webapplication.grade.response.GradeInfoListItemResponse;

/**
 * 등급 정책 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code GradeQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>등급 정책은 도메인 enum({@code MemberGrade}) 상수에서 파생되는 정적 목록이라 조회만 있고
 * 쓰기 경로가 없다. 따라서 CommandUseCase 짝을 두지 않는다.
 */
public interface GradeQueryUseCase {

    List<GradeInfoListItemResponse> getGradeInfoList();
}
