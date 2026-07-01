package com.tastyhouse.core.domain.banner.domain.repository;

import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import java.util.Optional;

public interface BannerRepository {

    PageResult<BannerListItemDto> findAllByType(BannerType type, PageQuery pageQuery);

    Optional<Banner> findById(Long id);
}
