package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.ceo.port.out.write.CeoLoadPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;

@Service
public class ShopCeoAssignmentService {

    private final ShopLoadPort shopLoadPort;
    private final ShopSavePort shopSavePort;
    private final CeoLoadPort ceoLoadPort;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopCeoAssignmentService(
        ShopLoadPort shopLoadPort,
        ShopSavePort shopSavePort,
        CeoLoadPort ceoLoadPort,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopLoadPort = shopLoadPort;
        this.shopSavePort = shopSavePort;
        this.ceoLoadPort = ceoLoadPort;
        this.shopCeoAssignmentRecorder = shopCeoAssignmentRecorder;
    }

    public void assign(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        Shop shop = loadShop(shopId);
        validateCeoExists(ceoId);

        CeoId currentCeoId = shop.getCeoId();
        if (ceoId.equals(currentCeoId)) {
            throw new ApplicationException(AdminErrorCode.SHOP_CEO_ALREADY_ASSIGNED);
        }

        shop.assignCeo(ceoId);
        shopSavePort.save(shop);

        if (currentCeoId != null) {
            shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
        }
        shopCeoAssignmentRecorder.recordGrant(shopId, ceoId, actorAdminId);
    }

    public void revoke(ShopId shopId, Long actorAdminId) {
        Shop shop = loadShop(shopId);

        CeoId currentCeoId = shop.getCeoId();
        if (currentCeoId == null) {
            throw new ApplicationException(AdminErrorCode.SHOP_CEO_NOT_ASSIGNED);
        }

        shop.assignCeo(null);
        shopSavePort.save(shop);

        shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
    }

    private Shop loadShop(ShopId shopId) {
        return shopLoadPort.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }

    private void validateCeoExists(CeoId ceoId) {
        if (ceoLoadPort.findById(ceoId).isEmpty()) {
            throw new ResourceNotFoundException(AdminErrorCode.CEO_NOT_FOUND);
        }
    }
}
