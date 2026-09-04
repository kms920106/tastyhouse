package com.tastyhouse.application.banner.port.out;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 배너 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>회원에게 노출되는 배너만 다룬다. 관리 화면 조회는 {@code BannerManagementQueryPort}가 소유한다 —
 * 두 화면은 같은 테이블을 보지만 계약이 겹치지 않아(공유 메서드 0개) 소비 앱별로 인터페이스를 나눈다.
 *
 * <p>구현은 infrastructure의 {@code BannerQueryDao} 하나가 두 포트를 함께 담당하므로 투영 코드는
 * 복제되지 않는다.
 */
public interface BannerQueryPort {

    PageResult<BannerListItemResult> findVisibleBannersByType(BannerType type, PageQuery pageQuery);
}
