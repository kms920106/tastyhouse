package com.tastyhouse.core.domain.banner.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.banner.application.dto.BannerSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerListItemResult;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface BannerRepository {

    PageResult<BannerListItemResult> findAllByType(BannerType type, PageQuery pageQuery);

    PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, PageQuery pageQuery);

    Optional<Banner> findById(BannerId id);

    Banner save(Banner banner);
}
