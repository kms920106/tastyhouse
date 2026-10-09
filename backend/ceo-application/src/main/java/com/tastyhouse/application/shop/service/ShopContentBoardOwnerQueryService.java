package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopMediaOwnerQueryPort;

@Service
@Transactional(readOnly = true)
class ShopContentBoardOwnerQueryService implements ShopContentBoardOwnerQueryUseCase {

    private final ShopMediaOwnerQueryPort shopMediaOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopContentBoardOwnerQueryService(ShopMediaOwnerQueryPort shopMediaOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopMediaOwnerQueryPort = shopMediaOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopContentBoardResult> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopMediaOwnerQueryPort.findContentBoards(shopId);
    }

}
