package com.tastyhouse.application.banner.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface BannerQueryPort {

    PageResult<BannerListItemResult> findVisibleBannersByType(String type, PageQuery pageQuery);
}
