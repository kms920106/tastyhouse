package com.tastyhouse.application.notice.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 공지사항 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code NoticeQueryDao}가 이를 구현한다. 이전에는 소비 모듈(web-api/admin-api)의
 * {@code NoticeQueryService}가 infra DAO를 직접 주입해 의존 방향이 api → infra로 흘렀고,
 * 그 결과 조회 계약의 소유권이 어댑터에 있었다. 포트를 여기 두면 의존이 역전되어
 * 어댑터 교체가 응용 계층에 파급되지 않는다.
 *
 * <p>반환 DTO({@code *Result})와 검색 조건({@code NoticeSearchCondition})도 같은 패키지가 소유한다 —
 * 포트만 옮기고 DTO를 infra에 남기면 소비 모듈이 여전히 infra를 import하므로 역전이 이름뿐이다.
 *
 * <p>메서드명은 DAO의 기존 이름을 그대로 승계한다(admin 마커 없이 순수 동작명 —
 * {@code findAllNotices}는 비노출 포함 전체, {@code findVisibleNotices}는 노출분만).
 */
public interface NoticeQueryPort {

    /**
     * 관리 목록 조회 — 비노출 공지를 포함하며 title/content 부분일치·visible 필터를 적용한다.
     */
    PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery);

    /**
     * 관리 상세 조회 — 비노출 공지도 조회된다.
     */
    Optional<NoticeDetailResult> findDetailById(Long id);

    /**
     * 회원 노출 목록 조회 — 노출(visible=true) 공지만 조회한다.
     */
    PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery);
}
