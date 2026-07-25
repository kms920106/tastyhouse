package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhoneNumber;
import com.tastyhouse.core.domain.shop.domain.repository.ShopPhoneNumberRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopPhoneNumberCreateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopPhoneNumberCommandService {

    private static final int MAX_PHONE_NUMBER_COUNT = 10;

    private final ShopPhoneNumberRepository shopPhoneNumberRepository;
    private final ShopRepository shopRepository;

    public Long addPhoneNumber(ShopPhoneNumberCreateCommand command) {
        List<ShopPhoneNumber> existingPhoneNumbers = shopPhoneNumberRepository.findByShopId(command.shopId());
        if (existingPhoneNumbers.size() >= MAX_PHONE_NUMBER_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_PHONE_NUMBER_LIMIT_EXCEEDED);
        }

        boolean primary = existingPhoneNumbers.isEmpty();
        ShopPhoneNumber phoneNumber = ShopPhoneNumber.of(command.shopId(), command.phoneNumber(), primary, command.virtual());
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(phoneNumber);

        if (primary) {
            syncShopPhoneNumber(command.shopId(), saved.getPhoneNumber());
        }

        return saved.getId();
    }

    public void deletePhoneNumber(Long id) {
        ShopPhoneNumber phoneNumber = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));
        shopPhoneNumberRepository.deleteById(id);

        if (!phoneNumber.isPrimary()) {
            return;
        }

        List<ShopPhoneNumber> remainingPhoneNumbers = shopPhoneNumberRepository.findByShopId(phoneNumber.getShopId());
        if (remainingPhoneNumbers.isEmpty()) {
            return;
        }

        ShopPhoneNumber newPrimary = remainingPhoneNumbers.getFirst();
        newPrimary.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(newPrimary);
        syncShopPhoneNumber(phoneNumber.getShopId(), saved.getPhoneNumber());
    }

    public void designatePrimary(Long id) {
        ShopPhoneNumber target = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));

        List<ShopPhoneNumber> phoneNumbers = shopPhoneNumberRepository.findByShopId(target.getShopId());
        for (ShopPhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.isPrimary() && !phoneNumber.getId().equals(target.getId())) {
                phoneNumber.unmarkPrimary();
                shopPhoneNumberRepository.save(phoneNumber);
            }
        }

        target.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(target);
        syncShopPhoneNumber(saved.getShopId(), saved.getPhoneNumber());
    }

    private void syncShopPhoneNumber(Long shopId, String phoneNumber) {
        ShopId targetShopId = ShopId.of(shopId);
        Shop shop = shopRepository.findById(targetShopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.changePhoneNumber(phoneNumber);
        shopRepository.save(shop);
    }
}
