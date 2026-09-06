package com.tastyhouse.application.banner.port.out;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface BannerQueryPort {

    PageResult<BannerListItemResult> findVisibleBannersByType(BannerType type, PageQuery pageQuery);
}
