package com.tastyhouse.core.domain.banner.application;

import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BannerQueryService {

    private final BannerRepository bannerRepository;

    public Page<BannerListItemDto> findHomeBanners(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bannerRepository.findAllByType(BannerType.HOME, pageable);
    }

    public Page<BannerListItemDto> findSidebarBanners(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bannerRepository.findAllByType(BannerType.SIDEBAR, pageable);
    }

    public Banner findById(Long id) {
        return bannerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
