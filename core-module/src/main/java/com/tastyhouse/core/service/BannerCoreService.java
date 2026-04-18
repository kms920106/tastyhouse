package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.banner.Banner;
import com.tastyhouse.core.entity.banner.BannerType;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.banner.BannerJpaRepository;
import com.tastyhouse.core.repository.banner.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerCoreService {

    private final BannerRepository bannerRepository;
    private final BannerJpaRepository bannerJpaRepository;

    @Transactional(readOnly = true)
    public Page<BannerListItemDto> findHomeBanners(int page, int size) {
        return bannerRepository.findAllByType(BannerType.HOME, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<BannerListItemDto> findSidebarBanners(int page, int size) {
        return bannerRepository.findAllByType(BannerType.SIDEBAR, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Banner findById(Long id) {
        return bannerJpaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
