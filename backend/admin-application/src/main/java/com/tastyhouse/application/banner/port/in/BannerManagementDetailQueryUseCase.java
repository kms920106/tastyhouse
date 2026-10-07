package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerDetailResult;

public interface BannerManagementDetailQueryUseCase {

    BannerDetailResult getBanner(Long id);
}
