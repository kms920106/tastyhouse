package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.repository.ShopHygieneBadgeRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopHygieneBadgeResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopHygieneBadgeQueryService {

    private final ShopHygieneBadgeRepository shopHygieneBadgeRepository;

    public List<ShopHygieneBadgeResult> findByShopId(Long shopId) {
        return shopHygieneBadgeRepository.findByShopId(shopId).stream()
            .map(ShopHygieneBadgeResult::from)
            .toList();
    }
}
