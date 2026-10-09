package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;

@Service
public class ShopPhoneNumberRegistryService {

    private static final int MAX_PHONE_NUMBER_COUNT = 10;

    private final ShopPhoneNumberLoadPort shopPhoneNumberLoadPort;
    private final ShopPhoneNumberSavePort shopPhoneNumberSavePort;
    private final ShopLoadPort shopLoadPort;
    private final ShopSavePort shopSavePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopPhoneNumberRegistryService(
        ShopPhoneNumberLoadPort shopPhoneNumberLoadPort,
        ShopPhoneNumberSavePort shopPhoneNumberSavePort,
        ShopLoadPort shopLoadPort,
        ShopSavePort shopSavePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopPhoneNumberLoadPort = shopPhoneNumberLoadPort;
        this.shopPhoneNumberSavePort = shopPhoneNumberSavePort;
        this.shopLoadPort = shopLoadPort;
        this.shopSavePort = shopSavePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long addPhoneNumber(Long shopId, String phoneNumber, boolean virtual, ShopChangeActor actor) {
        List<ShopPhoneNumber> existingPhoneNumbers = shopPhoneNumberLoadPort.findByShopId(shopId);
        if (existingPhoneNumbers.size() >= MAX_PHONE_NUMBER_COUNT) {
            throw new ApplicationException(CeoErrorCode.SHOP_PHONE_NUMBER_LIMIT_EXCEEDED);
        }

        boolean primary = existingPhoneNumbers.isEmpty();
        ShopPhoneNumber saved = shopPhoneNumberSavePort.save(
            ShopPhoneNumber.of(ShopId.of(shopId), phoneNumber, primary, virtual)
        );

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.PHONE_NUMBER,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describePhoneNumber(saved)
        );

        if (primary) {
            syncShopPhoneNumber(shopId, saved.getPhoneNumber());
            shopChangeHistoryRecorder.record(
                saved.getShopId(),
                ShopChangeType.REPRESENTATIVE_PHONE,
                ShopChangeActionType.UPDATE,
                actor,
                describeRepresentativePhone(null),
                describeRepresentativePhone(saved.getPhoneNumber())
            );
        }

        return saved.getId();
    }

    public void deletePhoneNumber(Long id, ShopChangeActor actor) {
        ShopPhoneNumber phoneNumber = shopPhoneNumberLoadPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));
        shopPhoneNumberSavePort.deleteById(id);

        shopChangeHistoryRecorder.record(
            phoneNumber.getShopId(),
            ShopChangeType.PHONE_NUMBER,
            ShopChangeActionType.DELETE,
            actor,
            describePhoneNumber(phoneNumber),
            null
        );

        if (!phoneNumber.isPrimary()) {
            return;
        }

        List<ShopPhoneNumber> remainingPhoneNumbers = shopPhoneNumberLoadPort.findByShopId(phoneNumber.getShopId().value());
        if (remainingPhoneNumbers.isEmpty()) {
            return;
        }

        ShopPhoneNumber newPrimary = remainingPhoneNumbers.getFirst();
        newPrimary.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberSavePort.save(newPrimary);
        syncShopPhoneNumber(phoneNumber.getShopId().value(), saved.getPhoneNumber());

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.REPRESENTATIVE_PHONE,
            ShopChangeActionType.UPDATE,
            actor,
            describeRepresentativePhone(phoneNumber.getPhoneNumber()),
            describeRepresentativePhone(saved.getPhoneNumber())
        );
    }

    public void designatePrimary(Long id, ShopChangeActor actor) {
        ShopPhoneNumber target = shopPhoneNumberLoadPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));

        List<ShopPhoneNumber> phoneNumbers = shopPhoneNumberLoadPort.findByShopId(target.getShopId().value());
        String previousPrimaryPhoneNumber = null;
        for (ShopPhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.isPrimary() && !phoneNumber.getId().equals(target.getId())) {
                previousPrimaryPhoneNumber = phoneNumber.getPhoneNumber();
                phoneNumber.unmarkPrimary();
                shopPhoneNumberSavePort.save(phoneNumber);
            }
        }

        target.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberSavePort.save(target);
        syncShopPhoneNumber(saved.getShopId().value(), saved.getPhoneNumber());

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.REPRESENTATIVE_PHONE,
            ShopChangeActionType.UPDATE,
            actor,
            describeRepresentativePhone(previousPrimaryPhoneNumber),
            describeRepresentativePhone(saved.getPhoneNumber())
        );
    }

    private String describePhoneNumber(ShopPhoneNumber phoneNumber) {
        if (phoneNumber.isVirtual()) {
            return phoneNumber.getPhoneNumber() + " (가상번호)";
        }
        return phoneNumber.getPhoneNumber();
    }

    private String describeRepresentativePhone(String phoneNumber) {
        return "대표번호: " + (phoneNumber == null ? ShopChangeValueFormatter.unset() : phoneNumber);
    }

    private void syncShopPhoneNumber(Long shopId, String phoneNumber) {
        ShopId targetShopId = ShopId.of(shopId);
        Shop shop = shopLoadPort.findById(targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
        shop.changePhoneNumber(phoneNumber);
        shopSavePort.save(shop);
    }
}
