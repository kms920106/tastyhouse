package com.tastyhouse.application.banner.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface BannerManagementQueryPort {

    PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, PageQuery pageQuery);

    Optional<BannerDetailResult> findDetailById(Long id);
}
