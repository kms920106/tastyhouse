package com.tastyhouse.core.domain.banner.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.banner.application.dto.BannerSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerDetailResult;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerListItemResult;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerManagementListItemResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BannerQueryService {

    private final BannerRepository bannerRepository;

    public PageResult<BannerListItemResult> findHomeBanners(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerRepository.findAllByType(BannerType.HOME, pageQuery);
    }

    public PageResult<BannerListItemResult> findSidebarBanners(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerRepository.findAllByType(BannerType.SIDEBAR, pageQuery);
    }

    public Banner findById(BannerId id) {
        return bannerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }

    public PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerRepository.findAllBanners(condition, pageQuery);
    }

    public BannerDetailResult findDetailById(BannerId id) {
        return BannerDetailResult.from(findById(id));
    }
}
