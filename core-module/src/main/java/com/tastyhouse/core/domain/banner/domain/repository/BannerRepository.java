package com.tastyhouse.core.domain.banner.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface BannerRepository {

    PageResult<BannerListItemDto> findAllByType(BannerType type, PageQuery pageQuery);

    PageResult<BannerAdminListItemDto> findAllBanners(BannerAdminSearchCondition condition, PageQuery pageQuery);

    Optional<Banner> findById(BannerId id);

    Banner save(Banner banner);
}
