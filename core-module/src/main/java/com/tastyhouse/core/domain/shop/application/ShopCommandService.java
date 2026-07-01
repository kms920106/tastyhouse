package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.infrastructure.persistence.ShopBookmarkJpaRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopCommandService {

    private final ShopQueryService shopQueryService;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final ShopBookmarkJpaRepository shopBookmarkJpaRepository;

    public boolean toggleBookmark(Long shopId, Long memberId) {
        if (shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId)) {
            shopBookmarkRepository.deleteByShopIdAndMemberId(shopId, memberId);
            return false;
        } else {
            shopQueryService.findShopById(shopId);
            shopBookmarkJpaRepository.save(new ShopBookmark(shopId, memberId));
            return true;
        }
    }
}
