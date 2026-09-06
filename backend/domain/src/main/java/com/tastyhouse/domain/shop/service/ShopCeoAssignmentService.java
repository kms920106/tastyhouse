package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopCeoAssignmentService {
    private final ShopRepository shopRepository;
    private final CeoRepository ceoRepository;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopCeoAssignmentService(
        ShopRepository shopRepository,
        CeoRepository ceoRepository,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopRepository = shopRepository;
        this.ceoRepository = ceoRepository;
        this.shopCeoAssignmentRecorder = shopCeoAssignmentRecorder;
    }

    public void assign(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        Shop shop = loadShop(shopId);
        validateCeoExists(ceoId);

        CeoId currentCeoId = shop.getCeoId();
        if (ceoId.equals(currentCeoId)) {
            throw new BusinessException(ErrorCode.SHOP_CEO_ALREADY_ASSIGNED);
        }

        shop.assignCeo(ceoId);
        shopRepository.save(shop);

        if (currentCeoId != null) {
            shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
        }
        shopCeoAssignmentRecorder.recordGrant(shopId, ceoId, actorAdminId);
    }

    public void revoke(ShopId shopId, Long actorAdminId) {
        Shop shop = loadShop(shopId);

        CeoId currentCeoId = shop.getCeoId();
        if (currentCeoId == null) {
            throw new BusinessException(ErrorCode.SHOP_CEO_NOT_ASSIGNED);
        }

        shop.assignCeo(null);
        shopRepository.save(shop);

        shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
    }

    private Shop loadShop(ShopId shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private void validateCeoExists(CeoId ceoId) {
        if (ceoRepository.findById(ceoId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.CEO_NOT_FOUND);
        }
    }
}
