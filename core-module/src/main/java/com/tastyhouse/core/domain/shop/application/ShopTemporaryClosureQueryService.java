package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopTemporaryClosureResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopTemporaryClosureQueryService {

    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;

    public List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId) {
        return shopTemporaryClosureRepository.findByShopId(shopId)
            .stream()
            .map(ShopTemporaryClosureResult::from)
            .toList();
    }
}
