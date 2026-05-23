package com.tastyhouse.core.domain.banner.domain.repository;

import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BannerRepository {

    Page<BannerListItemDto> findAllByType(BannerType type, Pageable pageable);

    Optional<Banner> findById(Long id);
}
