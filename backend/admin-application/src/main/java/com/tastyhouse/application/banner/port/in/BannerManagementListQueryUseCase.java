package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface BannerManagementListQueryUseCase {

    PageResult<BannerManagementListItemResult> getBanners(String type, String title, Boolean visible, int page, int size);
}
