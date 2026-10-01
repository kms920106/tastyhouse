package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

@AdminApp
public class ShopCeoAssignmentService {

    private final ShopPersistencePort shopPersistencePort;
    private final CeoPersistencePort ceoPersistencePort;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopCeoAssignmentService(
        ShopPersistencePort shopPersistencePort,
        CeoPersistencePort ceoPersistencePort,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopPersistencePort = shopPersistencePort;
        this.ceoPersistencePort = ceoPersistencePort;
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
        shopPersistencePort.save(shop);

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
        shopPersistencePort.save(shop);

        shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
    }

    private Shop loadShop(ShopId shopId) {
        return shopPersistencePort.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private void validateCeoExists(CeoId ceoId) {
        if (ceoPersistencePort.findById(ceoId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.CEO_NOT_FOUND);
        }
    }
}
