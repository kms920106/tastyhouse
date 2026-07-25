package com.tastyhouse.core.domain.shop.application;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopConvenienceInfoResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopConvenienceInfoQueryService {

    private final ShopConvenienceInfoRepository shopConvenienceInfoRepository;

    public Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId) {
        return shopConvenienceInfoRepository.findByShopId(shopId)
            .map(ShopConvenienceInfoResult::from);
    }
}
