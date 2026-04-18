package com.tastyhouse.core.repository.banner;

import com.tastyhouse.core.entity.banner.BannerType;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BannerRepository {

    Page<BannerListItemDto> findAllByType(BannerType type, Pageable pageable);
}
