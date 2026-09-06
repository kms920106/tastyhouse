package com.tastyhouse.domain.shop.service;

import java.util.List;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopPhoneNumberRegistryService {
    private static final int MAX_PHONE_NUMBER_COUNT = 10;

    private final ShopPhoneNumberRepository shopPhoneNumberRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopPhoneNumberRepository = shopPhoneNumberRepository;
        this.shopRepository = shopRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long addPhoneNumber(Long shopId, String phoneNumber, boolean virtual, ShopChangeActor actor) {
        List<ShopPhoneNumber> existingPhoneNumbers = shopPhoneNumberRepository.findByShopId(shopId);
        if (existingPhoneNumbers.size() >= MAX_PHONE_NUMBER_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_PHONE_NUMBER_LIMIT_EXCEEDED);
        }

        boolean primary = existingPhoneNumbers.isEmpty();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(
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
        ShopPhoneNumber phoneNumber = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));
        shopPhoneNumberRepository.deleteById(id);

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

        List<ShopPhoneNumber> remainingPhoneNumbers = shopPhoneNumberRepository.findByShopId(phoneNumber.getShopId().value());
        if (remainingPhoneNumbers.isEmpty()) {
            return;
        }

        ShopPhoneNumber newPrimary = remainingPhoneNumbers.getFirst();
        newPrimary.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(newPrimary);
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
        ShopPhoneNumber target = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));

        List<ShopPhoneNumber> phoneNumbers = shopPhoneNumberRepository.findByShopId(target.getShopId().value());
        String previousPrimaryPhoneNumber = null;
        for (ShopPhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.isPrimary() && !phoneNumber.getId().equals(target.getId())) {
                previousPrimaryPhoneNumber = phoneNumber.getPhoneNumber();
                phoneNumber.unmarkPrimary();
                shopPhoneNumberRepository.save(phoneNumber);
            }
        }

        target.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(target);
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
        Shop shop = shopRepository.findById(targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.changePhoneNumber(phoneNumber);
        shopRepository.save(shop);
    }
}
