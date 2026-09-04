package com.tastyhouse.application.banner.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 배너 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>비노출·기간만료 배너를 포함한 관리 조회를 담당한다. 회원 노출 조회는 {@code BannerQueryPort}가
 * 소유한다. 포트명의 {@code Management}는 "누가(관리자)"가 아니라 "무엇을 위한 것인가(관리 화면)"를
 * 뜻하며, 이 포트가 반환하는 {@link BannerManagementListItemResult} 계열의 이름을 승계한 것이다.
 */
public interface BannerManagementQueryPort {

    PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, PageQuery pageQuery);

    Optional<BannerDetailResult> findDetailById(Long id);
}
