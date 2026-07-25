package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.repository.ShopSuspensionRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopSuspensionResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopSuspensionQueryService {

    private final ShopSuspensionRepository shopSuspensionRepository;

    public List<ShopSuspensionResult> findSuspensions(Long shopId) {
        return shopSuspensionRepository.findByShopId(shopId)
            .stream()
            .map(ShopSuspensionResult::from)
            .toList();
    }
}
