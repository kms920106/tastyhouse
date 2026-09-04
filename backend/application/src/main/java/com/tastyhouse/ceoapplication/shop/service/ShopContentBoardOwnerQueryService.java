package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;

/**
 * 점주용 가게 콘텐츠보드 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopContentBoardOwnerQueryService implements ShopContentBoardOwnerQueryUseCase {

    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopContentBoardOwnerQueryService(ShopOwnerQueryPort shopOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopContentBoardResult> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopOwnerQueryPort.findContentBoards(shopId);
    }

}
