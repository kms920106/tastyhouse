package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.repository.ShopContentBoardRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopContentBoardResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopContentBoardQueryService {

    private final ShopContentBoardRepository shopContentBoardRepository;

    public List<ShopContentBoardResult> findContentBoards(Long shopId) {
        return shopContentBoardRepository.findByShopId(shopId)
            .stream()
            .map(ShopContentBoardResult::from)
            .toList();
    }
}
