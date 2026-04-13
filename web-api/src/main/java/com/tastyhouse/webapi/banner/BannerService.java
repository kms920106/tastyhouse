package com.tastyhouse.webapi.banner;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import com.tastyhouse.core.service.BannerCoreService;
import com.tastyhouse.webapi.banner.response.BannerListItem;
import com.tastyhouse.webapi.common.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCoreService bannerCoreService;

    @Transactional(readOnly = true)
    public PageResult<BannerListItem> searchBannerList(PageRequest pageRequest) {
        return bannerCoreService.findAllWithPagination(pageRequest.page(), pageRequest.size()).map(this::convertToBannerListItem);
    }

    private BannerListItem convertToBannerListItem(BannerListItemDto dto) {
        return BannerListItem.from(
            dto.id(),
            dto.title(),
            dto.imageUrl(),
            dto.linkUrl()
        );
    }
}
